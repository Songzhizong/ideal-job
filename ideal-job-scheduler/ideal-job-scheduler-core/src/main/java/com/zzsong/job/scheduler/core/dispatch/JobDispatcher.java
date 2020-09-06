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
import java.util.Collections;

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
    String executeParam;
    if (customExecuteParam == null) {
      executeParam = jobView.getExecuteParam();
    } else {
      executeParam = customExecuteParam;
    }
    Object[] paramTmp = new Object[1];
    JobInstance instance = createJobInstance(jobView, triggerType, executeParam);
    return handler.parseExecuteParam(executeParam)
        .map(param -> {
          paramTmp[0] = param;
          return handler.chooseWorkers(jobView, param);
        })
        .onErrorResume(e -> {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.info("任务: {} 执行异常: {}", jobView.getJobId(), errMsg);
          instance.setDispatchStatus(JobInstance.STATUS_FAIL);
          instance.setDispatchMsg(errMsg);
          instance.setHandleStatus(HandleStatusEnum.DISCARD);
          return Mono.just(Collections.emptyList());
        })
        .flatMap(servers -> {
          if (servers.size() == 0) {
            return instanceService.saveInstance(instance)
                .flatMap(i -> Mono.error(new VisibleException(i.getDispatchMsg())));
          }
          if (servers.size() == 1) {
            LbServer lbServer = servers.get(0);
            String instanceId = lbServer.getInstanceId();
            instance.setExecutorInstance(instanceId);
            return instanceService.saveInstance(instance)
                .flatMap(savedInstance ->
                    handler.execute(savedInstance, jobView, triggerType, paramTmp[0])
                );
          } else {
            return instanceService.saveInstance(instance)
                .flatMap(savedInstance -> {
                  Long instanceId = savedInstance.getInstanceId();
                  return Flux.fromIterable(servers)
                      .flatMap(server -> {
                        JobInstance child = createJobInstance(jobView, triggerType, executeParam);
                        child.setParentId(instanceId);
                        child.setExecutorInstance(server.getInstanceId());
                        return instanceService.saveInstance(instance)
                            .flatMap(savedChild ->
                                handler.execute(savedChild, jobView, triggerType, paramTmp[0])
                            );
                      }).collectList().map(list -> true);
                });
          }
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
    instance.setHandleTime(currentTimeMillis);
    instance.setHandleStatus(HandleStatusEnum.RUNNING);
    instance.setSequence(1);
    return instance;
  }

//  public Mono<Integer> dispatchCallback(@Nonnull TaskCallback taskCallback) {
//    int handleStatus = taskCallback.getHandleStatus();
//    TaskCallbackParam param = new TaskCallbackParam();
//    param.setInstanceId(taskCallback.getInstanceId());
//    param.setHandleTime(taskCallback.getHandleTime());
//    param.setFinishedTime(taskCallback.getFinishedTime());
//    param.setHandleStatus(HandleStatusEnum.valueOfCode(handleStatus));
//    param.setResult(taskCallback.getHandleMessage());
//    param.setSequence(taskCallback.getSequence());
//    param.setUpdateTime(DateTimes.now());
//    return instanceService.updateWhenTriggerCallback(param);
//  }
}
