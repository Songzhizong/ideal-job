package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl;

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum;
import cn.sh.ideal.job.common.constants.RouteStrategyEnum;
import cn.sh.ideal.job.common.constants.TriggerTypeEnum;
import cn.sh.ideal.job.common.exception.VisibleException;
import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;
import cn.sh.ideal.job.common.message.payload.TaskParam;
import cn.sh.ideal.job.common.transfer.CommonResMsg;
import cn.sh.ideal.job.common.worker.TaskWorker;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance;
import cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView;
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService;
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService;
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandler;
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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
    private final JobInstanceService jobInstanceService;
    @Nonnull
    private final JobExecutorService jobExecutorService;

    public JobHandlerExecuteHandler(@Nonnull LbFactory<TaskWorker> lbFactory,
                                    @Nonnull JobInstanceService jobInstanceService,
                                    @Nonnull JobExecutorService jobExecutorService) {
        this.lbFactory = lbFactory;
        this.jobInstanceService = jobInstanceService;
        this.jobExecutorService = jobExecutorService;
        ExecuteHandlerFactory.register(ExecuteTypeEnum.JOB_HANDLER, this);
    }

    @Override
    public void execute(@Nonnull JobInstance instance,
                        @Nonnull DispatchJobView jobView,
                        @Nonnull TriggerTypeEnum triggerType,
                        @Nullable String customExecuteParam) {
        List<TaskWorker> chooseWorkers = chooseWorkers(jobView);
        String executeParam = customExecuteParam;
        if (executeParam == null) {
            executeParam = jobView.getExecuteParam();
        }
        if (chooseWorkers.size() == 1) {
            TaskWorker taskWorker = chooseWorkers.get(0);
            TaskParam taskParam = new TaskParam();
            taskParam.setJobId(jobView.getJobId() + "");
            taskParam.setInstanceId(instance.getInstanceId());
            taskParam.setExecutorHandler(jobView.getExecutorHandler());
            taskParam.setExecuteParam(executeParam);
            taskParam.setBlockStrategy(jobView.getBlockStrategy().name());

            instance.setExecutorInstance(taskWorker.getInstanceId());
            try {
                taskWorker.execute(taskParam);
            } catch (Exception e) {
                String errMsg = e.getClass().getName() + ": " + e.getMessage();
                log.info("远程服务: {} 调用异常: {}", taskWorker.getInstanceId(), errMsg);
                throw new VisibleException(CommonResMsg.INTERNAL_SERVER_ERROR, errMsg);
            }
        } else {
            for (TaskWorker worker : chooseWorkers) {
                JobInstance jobInstance = JobInstance.createInitialized();
                jobInstance.setParentId(instance.getInstanceId());
                jobInstance.setJobId(jobView.getJobId());
                jobInstance.setExecutorId(jobView.getExecutorId());
                jobInstance.setTriggerType(triggerType);
                jobInstance.setSchedulerInstance(instance.getSchedulerInstance());
                jobInstance.setExecutorInstance(worker.getInstanceId());
                jobInstance.setExecutorHandler(jobView.getExecutorHandler());
                jobInstance.setExecuteParam(executeParam);
                jobInstanceService.saveInstance(jobInstance);

                TaskParam taskParam = new TaskParam();
                taskParam.setJobId(jobView.getJobId() + "");
                taskParam.setInstanceId(jobInstance.getInstanceId());
                taskParam.setExecutorHandler(jobView.getExecutorHandler());
                taskParam.setExecuteParam(executeParam);
                taskParam.setBlockStrategy(jobView.getBlockStrategy().name());
                try {
                    worker.execute(taskParam);
                } catch (Exception e) {
                    String errMsg = e.getClass().getName() + ": " + e.getMessage();
                    instance.setDispatchStatus(JobInstance.STATUS_FAIL);
                    jobInstance.setDispatchMsg(errMsg);
                    log.info("远程服务: {} 调用异常: {}", worker.getInstanceId(), errMsg);
                }
            }
        }
    }

    @Nonnull
    private List<TaskWorker> chooseWorkers(@Nonnull DispatchJobView jobView) {
        long jobId = jobView.getJobId();
        long executorId = jobView.getExecutorId();
        String executorHandler = jobView.getExecutorHandler();
        RouteStrategyEnum routeStrategy = jobView.getRouteStrategy();
        JobExecutor executor = jobExecutorService.loadById(executorId);
        if (executor == null) {
            log.info("任务: {} 调度失败, 执行器: {} 不存在", jobId, executorId);
            throw new VisibleException(CommonResMsg.NOT_FOUND, "执行器不存在");
        }
        String executorAppName = executor.getAppName();
        if (StringUtils.isBlank(executorAppName)) {
            log.info("任务: {} 调度失败, 执行器: {} 应用名称为空", jobId, executorId);
            throw new VisibleException("执行器应用名称为空");
        }
        if (StringUtils.isBlank(executorHandler)) {
            log.info("任务: {} 的执行处理器为空", jobId);
            throw new VisibleException("executorHandler为空");
        }
        LbServerHolder<TaskWorker> serverHolder = lbFactory.getServerHolder(executorAppName);
        List<TaskWorker> reachableServers = serverHolder.getReachableServers();
        if (reachableServers.isEmpty()) {
            log.info("执行器: {} 当前没有可用的实例", executorAppName);
            throw new VisibleException("执行器当前没有可用的实例");
        }
        if (routeStrategy == RouteStrategyEnum.BROADCAST) {
            return reachableServers;
        } else {
            LbStrategyEnum lbStrategy = routeStrategy.getLbStrategy();
            assert lbStrategy != null;
            LoadBalancer<TaskWorker> loadBalancer = lbFactory
                    .getLoadBalancer(executorAppName, lbStrategy);
            TaskWorker chooseServer = loadBalancer.chooseServer(jobId, reachableServers);
            if (chooseServer == null) {
                log.info("执行器: {} 选取实例为空", executorAppName);
                throw new VisibleException("执行器选取实例为空");
            }
            return Collections.singletonList(chooseServer);
        }
    }
}
