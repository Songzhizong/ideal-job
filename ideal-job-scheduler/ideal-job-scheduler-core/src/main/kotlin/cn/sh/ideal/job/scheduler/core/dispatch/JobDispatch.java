package cn.sh.ideal.job.scheduler.core.dispatch;

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum;
import cn.sh.ideal.job.common.constants.HandleStatusEnum;
import cn.sh.ideal.job.common.constants.TriggerTypeEnum;
import cn.sh.ideal.job.common.exception.VisibleException;
import cn.sh.ideal.job.common.message.payload.TaskCallback;
import cn.sh.ideal.job.common.utils.DateTimes;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance;
import cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView;
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService;
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerConfig;
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandler;
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Component
public class JobDispatch {
    private static final Logger log = LoggerFactory.getLogger(JobDispatch.class);
    @Nonnull
    private final JobInstanceService instanceService;
    @Nonnull
    private final JobSchedulerConfig jobSchedulerConfig;

    public JobDispatch(@Nonnull JobInstanceService instanceService,
                       @Nonnull JobSchedulerConfig jobSchedulerConfig) {
        this.instanceService = instanceService;
        this.jobSchedulerConfig = jobSchedulerConfig;
    }

    public void dispatch(@Nonnull DispatchJobView jobView,
                         @Nonnull TriggerTypeEnum triggerType,
                         @Nullable String customExecuteParam) {
        ExecuteTypeEnum executeType = jobView.getExecuteType();
        ExecuteHandler handler = ExecuteHandlerFactory.getHandler(executeType);
        if (handler == null) {
            log.error("triggerType: {} 没有对应的执行处理器", triggerType);
            throw new VisibleException("triggerType: $triggerType 没有对应的执行处理器");
        }
        JobInstance instance = JobInstance.createInitialized();
        instance.setJobId(jobView.getJobId());
        instance.setExecutorId(jobView.getExecutorId());
        instance.setTriggerType(triggerType);
        instance.setSchedulerInstance(jobSchedulerConfig.getIpPort());
        instance.setExecutorHandler(jobView.getExecutorHandler());
        if (customExecuteParam != null) {
            instance.setExecuteParam(customExecuteParam);
        } else {
            instance.setExecuteParam(jobView.getExecuteParam());
        }
        instanceService.saveInstance(instance);
        try {
            handler.execute(instance, jobView, triggerType, customExecuteParam);
        } catch (Exception e) {
            String errMsg = e.getClass().getName() + ": " + e.getMessage();
            instance.setDispatchStatus(JobInstance.STATUS_FAIL);
            instance.setDispatchMsg(errMsg);
            log.info("任务: {} 调度异常: {}", jobView.getJobId(), errMsg);
        } finally {
            instanceService.updateDispatchInfo(instance);
        }
    }

    public void dispatchCallback(@Nonnull TaskCallback taskCallback) {
        // cn.sh.ideal.job.scheduler.core.admin.repository.JobInstanceRepository.updateWhenTriggerCallback
        // triggerLog 设置参数务必和上述方法中的参数一致
        // 收到执行回调直接更新日志中变化的部分即可
        int handleStatus = taskCallback.getHandleStatus();
        JobInstance instance = new JobInstance();
        instance.setInstanceId(taskCallback.getInstanceId());
        instance.setHandleTime(taskCallback.getHandleTime());
        instance.setFinishedTime(taskCallback.getFinishedTime());
        instance.setHandleStatus(HandleStatusEnum.valueOfCode(handleStatus));
        instance.setResult(taskCallback.getHandleMessage());
        instance.setSequence(taskCallback.getSequence());
        instance.setUpdateTime(DateTimes.now());
        instanceService.updateWhenTriggerCallback(instance);
    }
}
