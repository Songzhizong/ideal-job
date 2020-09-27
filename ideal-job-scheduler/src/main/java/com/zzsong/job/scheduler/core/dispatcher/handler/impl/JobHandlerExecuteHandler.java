package com.zzsong.job.scheduler.core.dispatcher.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.loadbalancer.*;
import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.executor.TaskExecutor;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.pojo.JobExecutor;
import com.zzsong.job.scheduler.core.admin.service.JobExecutorService;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandler;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandlerFactory;
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
 * @author 宋志宗 on 2020/9/3
 */
@Component("jobHandlerExecuteHandler")
public final class JobHandlerExecuteHandler implements ExecuteHandler {
  private static final Logger log = LoggerFactory.getLogger(JobHandlerExecuteHandler.class);
  @Nonnull
  private final LbFactory<TaskExecutor> lbFactory;
  @Nonnull
  private final JobExecutorService jobExecutorService;

  public JobHandlerExecuteHandler(@Nonnull LbFactory<TaskExecutor> lbFactory,
                                  @Nonnull JobExecutorService jobExecutorService) {
    this.lbFactory = lbFactory;
    this.jobExecutorService = jobExecutorService;
    ExecuteHandlerFactory.register(ExecuteTypeEnum.BEAN, this);
  }

  @Nonnull
  @Override
  public Mono<Res<Void>> execute(@Nonnull LbServer lbServer,
                                 @Nonnull JobInstance instance,
                                 @Nonnull JobView jobView,
                                 @Nonnull Object executeParam) {
    TaskExecutor taskExecutor = (TaskExecutor) lbServer;
    final String param = (String) executeParam;
    TaskParam taskParam = new TaskParam();
    taskParam.setJobId(jobView.getJobId() + "");
    taskParam.setInstanceId(instance.getInstanceId());
    taskParam.setExecutorHandler(jobView.getExecutorHandler());
    taskParam.setExecuteParam(param);
    taskParam.setBlockStrategy(jobView.getBlockStrategy().name());
    return taskExecutor.execute(taskParam);
  }

  @Nonnull
  @Override
  public Object parseExecuteParam(@Nonnull String executeParam) {
    return executeParam;
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  @Override
  public Mono<List<? extends LbServer>> chooseExecutors(@Nonnull JobView jobView,
                                                        @Nonnull Object executeParam) {
    long jobId = jobView.getJobId();
    long executorId = jobView.getExecutorId();
    String executorHandler = jobView.getExecutorHandler();
    RouteStrategyEnum routeStrategy = jobView.getRouteStrategy();
    Mono<Optional<JobExecutor>> executorMono = jobExecutorService.loadById(executorId);
    return executorMono.flatMap(executorOptional -> {
      if (!executorOptional.isPresent()) {
        log.info("任务: {} 调度失败, 执行器: {} 不存在", jobId, executorId);
        return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND,
            "执行器: " + executorId + "不存在"));
      }
      JobExecutor executor = executorOptional.get();
      String appName = executor.getAppName();
      if (StringUtils.isBlank(appName)) {
        log.info("任务: {} 调度失败, 执行器: {} 应用名称为空", jobId, executorId);
        return Mono.error(new VisibleException("执行器应用名称为空"));
      }
      if (StringUtils.isBlank(executorHandler)) {
        log.info("任务: {} 的执行处理器为空", jobId);
        return Mono.error(new VisibleException("executorHandler为空"));
      }
      List<TaskExecutor> reachableServers = lbFactory.getReachableServers(appName);
      if (reachableServers.isEmpty()) {
        log.info("执行器: {} 当前没有可用的实例", appName);
        return Mono.error(new VisibleException("执行器: " + appName + " 当前没有可用的实例"));
      }
      if (routeStrategy == RouteStrategyEnum.BROADCAST) {
        return Mono.just(reachableServers);
      } else {
        LbStrategyEnum lbStrategy = routeStrategy.getLbStrategy();
        LoadBalancer<TaskExecutor> loadBalancer;
        if (lbStrategy == null) {
          loadBalancer = lbFactory.getLoadBalancer(appName);
        } else {
          loadBalancer = lbFactory.getLoadBalancer(appName, lbStrategy);
        }
        TaskExecutor chooseServer = loadBalancer.chooseServer(jobId, reachableServers);
        if (chooseServer == null) {
          log.info("执行器: {} 选取实例为空", appName);
          return Mono.error(new VisibleException("执行器选取实例为空"));
        }
        return Mono.just(Collections.singletonList(chooseServer));
      }
    });
  }
}
