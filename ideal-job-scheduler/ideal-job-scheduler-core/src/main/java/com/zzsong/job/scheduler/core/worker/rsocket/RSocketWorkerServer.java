package com.zzsong.job.scheduler.core.worker.rsocket;

import com.google.common.collect.ImmutableList;
import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.constants.WorkerRouter;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.message.payload.LoginMessage;
import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.conf.JobSchedulerProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/3
 */
@Controller
public class RSocketWorkerServer {
  private static final Logger log = LoggerFactory.getLogger(RSocketWorkerServer.class);
  @Nonnull
  private final LbFactory<TaskWorker> lbFactory;
  @Nonnull
  private final JobInstanceService instanceService;
  @Nonnull
  private final JobSchedulerProperties schedulerProperties;

  public RSocketWorkerServer(@Nonnull LbFactory<TaskWorker> lbFactory,
                             @Nonnull JobInstanceService instanceService,
                             @Nonnull JobSchedulerProperties schedulerProperties) {
    this.lbFactory = lbFactory;
    this.instanceService = instanceService;
    this.schedulerProperties = schedulerProperties;
  }

  /**
   * 客户端与服务端建立连接
   */
  @ConnectMapping(WorkerRouter.LOGIN)
  void login(@Nonnull RSocketRequester requester, @Payload String loginMessage) {
    String propertiesAccessToken = schedulerProperties.getAccessToken();
    LoginMessage message = JsonUtils.parseJson(loginMessage, LoginMessage.class);
    String instanceId = message.getInstanceId();
    String appName = message.getAppName();
    String accessToken = message.getAccessToken();
    int weight = message.getWeight();
    RSocketTaskWorker[] warp = new RSocketTaskWorker[1];
    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          if (StringUtils.isNotBlank(propertiesAccessToken)
              && !propertiesAccessToken.equals(accessToken)) {
            log.info("accessToken不合法");
            requester.route("interrupt")
                .data("accessToken不合法")
                .retrieveMono(String.class)
                .doOnNext(log::info)
                .subscribe();
          } else {
            log.info("{} 客户端: {} 建立连接.", appName, instanceId);
            RSocketTaskWorker worker
                = new RSocketTaskWorker(appName, instanceId, requester);
            warp[0] = worker;
            worker.setWeight(weight);
            lbFactory.addServers(appName, ImmutableList.of(worker));
          }
        })
        .doOnError(error -> {
          String errMessage = error.getClass().getName() +
              ": " + error.getMessage();
          log.info("socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          RSocketTaskWorker worker = warp[0];
          if (worker != null) {
            lbFactory.markServerDown(appName, worker);
          }
          log.info("{} 客户端: {} 断开连接: {}", appName, instanceId, consumer);
        })
        .subscribe();
//    try {
//      TimeUnit.SECONDS.sleep(1);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
  }

  /**
   * task执行完成回调
   */
  @MessageMapping(WorkerRouter.TASK_CALLBACK)
  Mono<Res<Void>> taskCallback(@Nonnull TaskCallback callback) {
    TaskResult taskResult = new TaskResult();
    taskResult.setInstanceId(callback.getInstanceId());
    taskResult.setHandleTime(callback.getHandleTime());
    taskResult.setFinishedTime(callback.getFinishedTime());
    taskResult.setHandleStatus(HandleStatusEnum.valueOfCode(callback.getHandleStatus()));
    taskResult.setResult(callback.getHandleResult());
    taskResult.setSequence(callback.getSequence());
    taskResult.setUpdateTime(DateTimes.now());
    return instanceService.updateByTaskResult(taskResult)
        .map(i -> Res.success());
  }
}
