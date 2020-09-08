package com.zzsong.job.scheduler.core.dispatch.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.loadbalancer.*;
import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.service.JobWorkerService;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandler;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("jobHandlerExecuteHandler")
public final class JobHandlerExecuteHandler implements ExecuteHandler {
  private static final Logger log = LoggerFactory.getLogger(JobHandlerExecuteHandler.class);
  @Nonnull
  private final LbFactory<TaskWorker> lbFactory;
  @Nonnull
  private final JobWorkerService jobWorkerService;

  public JobHandlerExecuteHandler(@Nonnull LbFactory<TaskWorker> lbFactory,
                                  @Nonnull JobWorkerService jobWorkerService) {
    this.lbFactory = lbFactory;
    this.jobWorkerService = jobWorkerService;
    ExecuteHandlerFactory.register(ExecuteTypeEnum.JOB_HANDLER, this);
  }

  @Nonnull
  @Override
  public Mono<Boolean> execute(@Nonnull LbServer lbServer,
                               @Nonnull JobInstance instance,
                               @Nonnull JobView jobView,
                               @Nonnull Object executeParam) {
    TaskWorker taskWorker = (TaskWorker) lbServer;
    final String param = (String) executeParam;
    TaskParam taskParam = new TaskParam();
    taskParam.setJobId(jobView.getJobId() + "");
    taskParam.setInstanceId(instance.getInstanceId());
    taskParam.setExecutorHandler(jobView.getExecutorHandler());
    taskParam.setExecuteParam(param);
    taskParam.setBlockStrategy(jobView.getBlockStrategy().name());
    return taskWorker.execute(taskParam)
        .map(res -> {
          if (res.isSuccess()) {
            return true;
          } else {
            log.info("远程服务: {} 调度失败: {}",
                taskWorker.getInstanceId(), res.getMessage());
            throw new VisibleException(res.getMessage());
          }
        });
  }

  @Nonnull
  @Override
  public Object parseExecuteParam(@Nonnull String executeParam) {
    return executeParam;
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  @Override
  public Mono<List<? extends LbServer>> chooseWorkers(@Nonnull JobView jobView,
                                                      @Nonnull Object executeParam) {
    long jobId = jobView.getJobId();
    long workerId = jobView.getWorkerId();
    String executorHandler = jobView.getExecutorHandler();
    RouteStrategyEnum routeStrategy = jobView.getRouteStrategy();
    Mono<Optional<JobWorker>> workerMono = jobWorkerService.loadById(workerId);
    return workerMono.flatMap(workerOptional -> {
      if (!workerOptional.isPresent()) {
        log.info("任务: {} 调度失败, 执行器: {} 不存在", jobId, workerId);
        return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND,
            "执行器: " + workerId + "不存在"));
      }
      JobWorker worker = workerOptional.get();
      String appName = worker.getAppName();
      if (StringUtils.isBlank(appName)) {
        log.info("任务: {} 调度失败, 执行器: {} 应用名称为空", jobId, workerId);
        return Mono.error(new VisibleException("执行器应用名称为空"));
      }
      if (StringUtils.isBlank(executorHandler)) {
        log.info("任务: {} 的执行处理器为空", jobId);
        return Mono.error(new VisibleException("executorHandler为空"));
      }
      LbServerHolder<TaskWorker> serverHolder = lbFactory.getServerHolder(appName);
      List<TaskWorker> reachableServers = serverHolder.getReachableServers();
      if (reachableServers.isEmpty()) {
        log.info("执行器: {} 当前没有可用的实例", appName);
        return Mono.error(new VisibleException("执行器: " + appName + " 当前没有可用的实例"));
      }
      if (routeStrategy == RouteStrategyEnum.BROADCAST) {
        return Mono.just(reachableServers);
      } else {
        LbStrategyEnum lbStrategy = routeStrategy.getLbStrategy();
        LoadBalancer<TaskWorker> loadBalancer;
        if (lbStrategy == null) {
          loadBalancer = lbFactory.getLoadBalancer(appName);
        } else {
          loadBalancer = lbFactory.getLoadBalancer(appName, lbStrategy);
        }
        TaskWorker chooseServer = loadBalancer.chooseServer(jobId, reachableServers);
        if (chooseServer == null) {
          log.info("执行器: {} 选取实例为空", appName);
          return Mono.error(new VisibleException("执行器选取实例为空"));
        }
        return Mono.just(Collections.singletonList(chooseServer));
      }
    });
  }
}
