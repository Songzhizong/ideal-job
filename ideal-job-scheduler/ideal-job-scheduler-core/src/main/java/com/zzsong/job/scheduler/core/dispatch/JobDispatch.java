package com.zzsong.job.scheduler.core.dispatch;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.api.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.db.entity.JobInstanceDo;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.conf.JobSchedulerConfig;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandler;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

  public void dispatch(@Nonnull JobView jobView,
                       @Nonnull TriggerTypeEnum triggerType,
                       @Nullable String customExecuteParam) {
    ExecuteTypeEnum executeType = jobView.getExecuteType();
    ExecuteHandler handler = ExecuteHandlerFactory.getHandler(executeType);
    if (handler == null) {
      log.error("triggerType: {} 没有对应的执行处理器", executeType);
      throw new VisibleException("triggerType: " + executeType + " 没有对应的执行处理器");
    }
    JobInstanceDo instance = JobInstanceDo.createInitialized();
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
      instance.setDispatchStatus(JobInstanceDo.STATUS_FAIL);
      instance.setDispatchMsg(errMsg);
      log.info("任务: {} 调度异常: {}", jobView.getJobId(), errMsg);
    } finally {
      instanceService.updateDispatchInfo(instance);
    }
  }

  public void dispatchCallback(@Nonnull TaskCallback taskCallback) {
    // com.zzsong.job.scheduler.core.admin.repository.JobInstanceRepository.updateWhenTriggerCallback
    // triggerLog 设置参数务必和上述方法中的参数一致
    // 收到执行回调直接更新日志中变化的部分即可
    int handleStatus = taskCallback.getHandleStatus();
    JobInstanceDo instance = new JobInstanceDo();
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
