package com.zzsong.job.scheduler.core.dispatcher;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.conf.JobSchedulerConfig;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandler;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 本地集群调度器
 * <p>通过此调度器直接在当前节点完成调度</p>
 * <p>
 *
 * @author 宋志宗 on 2020/8/23
 */
@Component("localClusterDispatcher")
public class LocalClusterNode implements ClusterNode {
  private static final Logger log = LoggerFactory.getLogger(LocalClusterNode.class);
  @Nonnull
  private final String ipPort;
  @Nonnull
  private final JobSchedulerConfig config;
  @Nonnull
  private final JobInstanceService instanceService;

  public LocalClusterNode(@Nonnull JobSchedulerConfig config,
                          @Nonnull JobInstanceService instanceService) {
    this.config = config;
    this.instanceService = instanceService;
    this.ipPort = config.getIpPort();
  }

  @Override
  public Mono<Res<Void>> dispatch(@Nonnull JobView jobView,
                                  @Nonnull TriggerTypeEnum triggerType,
                                  @Nullable String customExecuteParam) {
    ExecuteTypeEnum executeType = jobView.getExecuteType();
    ExecuteHandler handler = ExecuteHandlerFactory.getHandler(executeType);
    if (handler == null) {
      log.error("triggerType: {} 没有对应的执行处理器", executeType);
      String message = "triggerType: " + executeType + " 没有对应的执行处理器";
      return Mono.just(Res.err(CommonResMsg.NOT_FOUND, message));
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
      instance.setDispatchStatus(JobInstance.DISPATCH_FAIL);
      instance.setDispatchMsg("执行参数解析异常: " + errMsg);
      instance.setHandleStatus(HandleStatusEnum.DISCARD);
      return instanceService.saveInstance(instance)
          .flatMap(i -> Mono.just(Res.err(i.getDispatchMsg())));
    }

    // 选取服务列表
    return handler.chooseWorkers(jobView, param)
        // 选取服务列表异常, 保存任务实例信息并抛出异常
        .onErrorResume(e -> {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.info("任务: {} 选取服务列表异常: {}", jobView.getJobId(), errMsg);
          instance.setDispatchStatus(JobInstance.DISPATCH_FAIL);
          instance.setDispatchMsg("选取服务列表异常: " + errMsg);
          instance.setHandleStatus(HandleStatusEnum.DISCARD);
          return instanceService.saveInstance(instance)
              .flatMap(i -> Mono.error(new VisibleException(i.getDispatchMsg())));
        })
        .flatMap(lbServers -> {
          // 服务列表为空, 抛出异常, 不应该到这一步的, chooseWorkers方法应保证返回的列表不为空, 否则应抛出异常
          if (lbServers.size() == 0) {
            log.error("任务: {} 选取的服务列表为空", jobView.getJobId());
            return Mono.just(Res.err("选取服务列表为空"));
          }
          // 选取服务列表只有一个, 直接执行
          if (lbServers.size() == 1) {
            LbServer lbServer = lbServers.get(0);
            String instanceId = lbServer.getInstanceId();
            instance.setExecutorInstance(instanceId);
            return instanceService.saveInstance(instance)
                .flatMap(savedInstance ->
                    handler.execute(lbServer, savedInstance, jobView, param)
                        // 调度异常应记录到任务实例信息中
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
                                    Res<Void> err = Res.err(CommonResMsg.INTERNAL_SERVER_ERROR, errMsg);
                                    return Mono.just(err);
                                  })
                          );
                    }).collectList().map(list -> Res.success());
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
    instance.setJobName(jobView.getJobName());
    instance.setWorkerId(jobView.getWorkerId());
    instance.setTriggerType(triggerType);
    instance.setSchedulerInstance(config.getIpPort());
    instance.setExecutorHandler(jobView.getExecutorHandler());
    instance.setExecuteParam(executeParam);
    instance.setCreatedTime(now);
    instance.setUpdateTime(now);
    instance.setDispatchStatus(JobInstance.DISPATCH_SUCCESS);
    instance.setDispatchMsg("Success");
    instance.setHandleTime(currentTimeMillis);
    instance.setHandleStatus(HandleStatusEnum.RUNNING);
    instance.setSequence(1);
    return instance;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return ipPort;
  }

  @Override
  public boolean heartbeat() {
    return true;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public int getWeight() {
    return 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalClusterNode that = (LocalClusterNode) o;
    return ipPort.equals(that.ipPort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipPort);
  }
}
