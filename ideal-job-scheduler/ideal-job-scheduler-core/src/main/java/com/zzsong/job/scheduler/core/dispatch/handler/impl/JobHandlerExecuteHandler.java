//package com.zzsong.job.scheduler.core.dispatch.handler.impl;
//
//import com.zzsong.job.common.constants.ExecuteTypeEnum;
//import com.zzsong.job.common.constants.RouteStrategyEnum;
//import com.zzsong.job.common.constants.TriggerTypeEnum;
//import com.zzsong.job.common.exception.VisibleException;
//import com.zzsong.job.common.loadbalancer.LbFactory;
//import com.zzsong.job.common.loadbalancer.LbServerHolder;
//import com.zzsong.job.common.loadbalancer.LbStrategyEnum;
//import com.zzsong.job.common.loadbalancer.LoadBalancer;
//import com.zzsong.job.common.message.payload.TaskParam;
//import com.zzsong.job.common.transfer.CommonResMsg;
//import com.zzsong.job.common.worker.TaskWorker;
//import com.zzsong.job.scheduler.core.admin.storage.param.DispatchResult;
//import com.zzsong.job.scheduler.core.pojo.JobInstance;
//import com.zzsong.job.scheduler.core.pojo.JobView;
//import com.zzsong.job.scheduler.core.pojo.JobWorker;
//import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInstanceDo;
//import com.zzsong.job.scheduler.core.admin.service.JobWorkerService;
//import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
//import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandler;
//import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
///**
// * @author 宋志宗
// * @date 2020/9/3
// */
//@Component("jobHandlerExecuteHandler")
//public final class JobHandlerExecuteHandler implements ExecuteHandler {
//  private static final Logger log = LoggerFactory.getLogger(JobHandlerExecuteHandler.class);
//  @Nonnull
//  private final LbFactory<TaskWorker> lbFactory;
//  @Nonnull
//  private final JobInstanceService jobInstanceService;
//  @Nonnull
//  private final JobWorkerService jobWorkerService;
//
//  public JobHandlerExecuteHandler(@Nonnull LbFactory<TaskWorker> lbFactory,
//                                  @Nonnull JobInstanceService jobInstanceService,
//                                  @Nonnull JobWorkerService jobWorkerService) {
//    this.lbFactory = lbFactory;
//    this.jobInstanceService = jobInstanceService;
//    this.jobWorkerService = jobWorkerService;
//    ExecuteHandlerFactory.register(ExecuteTypeEnum.JOB_HANDLER, this);
//  }
//
//  @Override
//  public Mono<Boolean> execute(@Nonnull JobView jobView,
//                               @Nonnull TriggerTypeEnum triggerType,
//                               @Nonnull String executeParam) {
//    List<TaskWorker> chooseWorkers = chooseWorkers(jobView);
//    if (chooseWorkers.size() == 1) {
//      TaskWorker taskWorker = chooseWorkers.get(0);
//      TaskParam taskParam = new TaskParam();
//      taskParam.setJobId(jobView.getJobId() + "");
//      taskParam.setInstanceId(instance.getInstanceId());
//      taskParam.setExecutorHandler(jobView.getExecutorHandler());
//      taskParam.setExecuteParam(executeParam);
//      taskParam.setBlockStrategy(jobView.getBlockStrategy().name());
//
//      instance.setExecutorInstance(taskWorker.getInstanceId());
//      try {
//        taskWorker.execute(taskParam);
//      } catch (Exception e) {
//        String errMsg = e.getClass().getName() + ": " + e.getMessage();
//        log.info("远程服务: {} 调用异常: {}", taskWorker.getInstanceId(), errMsg);
//        throw new VisibleException(CommonResMsg.INTERNAL_SERVER_ERROR, errMsg);
//      }
//    } else {
//      for (TaskWorker worker : chooseWorkers) {
//        JobInstanceDo jobInstance = JobInstanceDo.createInitialized();
//        jobInstance.setParentId(instance.getInstanceId());
//        jobInstance.setJobId(jobView.getJobId());
//        jobInstance.setWorkerId(jobView.getWorkerId());
//        jobInstance.setTriggerType(triggerType);
//        jobInstance.setSchedulerInstance(instance.getSchedulerInstance());
//        jobInstance.setExecutorInstance(worker.getInstanceId());
//        jobInstance.setExecutorHandler(jobView.getExecutorHandler());
//        jobInstance.setExecuteParam(executeParam);
//        jobInstanceService.saveInstance(jobInstance);
//
//        TaskParam taskParam = new TaskParam();
//        taskParam.setJobId(jobView.getJobId() + "");
//        taskParam.setInstanceId(jobInstance.getInstanceId());
//        taskParam.setExecutorHandler(jobView.getExecutorHandler());
//        taskParam.setExecuteParam(executeParam);
//        taskParam.setBlockStrategy(jobView.getBlockStrategy().name());
//        try {
//          worker.execute(taskParam);
//        } catch (Exception e) {
//          String errMsg = e.getClass().getName() + ": " + e.getMessage();
//          instance.setDispatchStatus(JobInstance.STATUS_FAIL);
//          jobInstance.setDispatchMsg(errMsg);
//          log.info("远程服务: {} 调用异常: {}", worker.getInstanceId(), errMsg);
//        }
//      }
//    }
//  }
//
//  @Nonnull
//  private List<TaskWorker> chooseWorkers(@Nonnull JobView jobView) {
//    long jobId = jobView.getJobId();
//    long workerId = jobView.getWorkerId();
//    String executorHandler = jobView.getExecutorHandler();
//    RouteStrategyEnum routeStrategy = jobView.getRouteStrategy();
//    Optional<JobWorker> block = jobWorkerService.loadById(workerId).block();
//    if (block == null || !block.isPresent()) {
//      log.info("任务: {} 调度失败, 执行器: {} 不存在", jobId, workerId);
//      throw new VisibleException(CommonResMsg.NOT_FOUND, "执行器不存在");
//    }
//    JobWorker executor = block.get();
//    String executorAppName = executor.getAppName();
//    if (StringUtils.isBlank(executorAppName)) {
//      log.info("任务: {} 调度失败, 执行器: {} 应用名称为空", jobId, workerId);
//      throw new VisibleException("执行器应用名称为空");
//    }
//    if (StringUtils.isBlank(executorHandler)) {
//      log.info("任务: {} 的执行处理器为空", jobId);
//      throw new VisibleException("executorHandler为空");
//    }
//    LbServerHolder<TaskWorker> serverHolder = lbFactory.getServerHolder(executorAppName);
//    List<TaskWorker> reachableServers = serverHolder.getReachableServers();
//    if (reachableServers.isEmpty()) {
//      log.info("执行器: {} 当前没有可用的实例", executorAppName);
//      throw new VisibleException("执行器当前没有可用的实例");
//    }
//    if (routeStrategy == RouteStrategyEnum.BROADCAST) {
//      return reachableServers;
//    } else {
//      LbStrategyEnum lbStrategy = routeStrategy.getLbStrategy();
//      assert lbStrategy != null;
//      LoadBalancer<TaskWorker> loadBalancer = lbFactory
//          .getLoadBalancer(executorAppName, lbStrategy);
//      TaskWorker chooseServer = loadBalancer.chooseServer(jobId, reachableServers);
//      if (chooseServer == null) {
//        log.info("执行器: {} 选取实例为空", executorAppName);
//        throw new VisibleException("执行器选取实例为空");
//      }
//      return Collections.singletonList(chooseServer);
//    }
//  }
//}
