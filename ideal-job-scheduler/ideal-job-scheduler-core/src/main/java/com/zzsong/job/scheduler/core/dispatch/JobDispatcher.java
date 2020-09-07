package com.zzsong.job.scheduler.core.dispatch;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.conf.JobSchedulerConfig;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandler;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Component
public class JobDispatcher {
  private static final Logger log = LoggerFactory.getLogger(JobDispatcher.class);
  @Nonnull
  private final JobSchedulerConfig config;
  @Nonnull
  private final JobInstanceService instanceService;

  public JobDispatcher(@Nonnull JobSchedulerConfig config,
                       @Nonnull JobInstanceService instanceService) {
    this.config = config;
    this.instanceService = instanceService;
  }

  public Mono<Boolean> dispatch(@Nonnull JobView jobView,
                                @Nonnull TriggerTypeEnum triggerType,
                                @Nullable String customExecuteParam) {
    ExecuteTypeEnum executeType = jobView.getExecuteType();
    ExecuteHandler handler = ExecuteHandlerFactory.getHandler(executeType);
    if (handler == null) {
      log.error("triggerType: {} 没有对应的执行处理器", executeType);
      return Mono.error(new VisibleException("triggerType: " + executeType + " 没有对应的执行处理器"));
    }
    // 如果自定义参数不为空则使用自定义参数, 反之使用任务默认参数
    String executeParam;
    if (customExecuteParam == null) {
      executeParam = jobView.getExecuteParam();
    } else {
      executeParam = customExecuteParam;
    }

    // 解析任务参数
    JobInstance instance = createJobInstance(jobView, triggerType, executeParam);
    Object param;
    try {
      param = handler.parseExecuteParam(executeParam);
    } catch (Exception e) {
      String errMsg = e.getClass().getName() + ": " + e.getMessage();
      log.info("任务: {} 执行参数解析异常: {}", jobView.getJobId(), errMsg);
      instance.setDispatchStatus(JobInstance.STATUS_FAIL);
      instance.setDispatchMsg("执行参数解析异常: " + errMsg);
      instance.setHandleStatus(HandleStatusEnum.DISCARD);
      return instanceService.saveInstance(instance)
          .flatMap(i -> Mono.error(new VisibleException(i.getDispatchMsg())));
    }

    // 选取服务列表
    return handler.chooseWorkers(jobView, param)
        // 选取服务列表异常, 保存任务实例信息并抛出异常
        .onErrorResume(e -> {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.info("任务: {} 选取服务列表异常: {}", jobView.getJobId(), errMsg);
          instance.setDispatchStatus(JobInstance.STATUS_FAIL);
          instance.setDispatchMsg("选取服务列表异常: " + errMsg);
          instance.setHandleStatus(HandleStatusEnum.DISCARD);
          return instanceService.saveInstance(instance)
              .flatMap(i -> Mono.error(new VisibleException(i.getDispatchMsg())));
        })
        .flatMap(lbServers -> {
          // 服务列表为空, 抛出异常, 不应该到这一步的, chooseWorkers方法应保证返回的列表不为空, 否则应抛出异常
          if (lbServers.size() == 0) {
            log.error("任务: {} 选取的服务列表为空", jobView.getJobId());
            return Mono.error(new VisibleException("选取服务列表为空"));
          }
          // 选取服务列表只有一个, 直接执行
          if (lbServers.size() == 1) {
            LbServer lbServer = lbServers.get(0);
            String instanceId = lbServer.getInstanceId();
            instance.setExecutorInstance(instanceId);
            return instanceService.saveInstance(instance)
                .flatMap(savedInstance ->
                    handler.execute(lbServer, savedInstance, jobView, param)
                        .doOnError(e -> {
                          String errMsg = e.getClass().getName() + ": " + e.getMessage();
                          log.info("任务实例: {} 执行异常: {}",
                              savedInstance.getInstanceId(), errMsg);
                        })
                );
          }
          // 选取服务列表为多个, 为每次调用创建任务实例并执行, 先保存父任务实例
          return instanceService.saveInstance(instance)
              .flatMap(savedInstance -> {
                long instanceId = savedInstance.getInstanceId();
                return Flux.fromIterable(lbServers)
                    .flatMap(server -> {
                      // 为每次调用创建任务实例
                      JobInstance child = createJobInstance(jobView, triggerType, executeParam);
                      child.setParentId(instanceId);
                      child.setExecutorInstance(server.getInstanceId());
                      // 保存任务实例并执行任务调用
                      return instanceService.saveInstance(instance)
                          .flatMap(savedChild ->
                              handler.execute(server, savedChild, jobView, param)
                                  .onErrorResume(e -> {
                                    String errMsg = e.getClass().getName() + ": " + e.getMessage();
                                    log.info("任务实例: {} 执行异常: {}",
                                        savedChild.getInstanceId(), errMsg);
                                    return Mono.just(false);
                                  })
                          );
                    }).collectList().map(list -> true);
              });
        });
  }

  /**
   * <pre>
   *   executorInstance
   *   dispatchStatus
   *   dispatchMsg
   *   handleStatus
   *   result
   * </pre>
   */
  @Nonnull
  private JobInstance createJobInstance(@Nonnull JobView jobView,
                                        @Nonnull TriggerTypeEnum triggerType,
                                        String executeParam) {
    long currentTimeMillis = System.currentTimeMillis();
    LocalDateTime now = DateTimes.now();
    JobInstance instance = new JobInstance();
    instance.setJobId(jobView.getJobId());
    instance.setWorkerId(jobView.getWorkerId());
    instance.setTriggerType(triggerType);
    instance.setSchedulerInstance(config.getIpPort());
    instance.setExecutorHandler(jobView.getExecutorHandler());
    instance.setExecuteParam(executeParam);
    instance.setCreatedTime(now);
    instance.setUpdateTime(now);
    instance.setDispatchStatus(JobInstance.STATUS_SUCCESS);
    instance.setDispatchMsg("Success");
    instance.setHandleTime(currentTimeMillis);
    instance.setHandleStatus(HandleStatusEnum.RUNNING);
    instance.setSequence(1);
    return instance;
  }
}
