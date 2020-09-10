package com.zzsong.job.scheduler.core.admin.storage.converter;

import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInstanceDo;
import com.zzsong.job.scheduler.core.pojo.JobInstance;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/26
 */
@SuppressWarnings("DuplicatedCode")
public final class JobInstanceDoConverter {

  @Nonnull
  public static JobInstanceDo fromJobInstance(@Nonnull JobInstance jobInstance) {
    JobInstanceDo jobInstanceDo = new JobInstanceDo();
    Long instanceId = jobInstance.getInstanceId();
    //noinspection ConstantConditions
    if (instanceId != null && instanceId > 0) {
      jobInstanceDo.setInstanceId(instanceId);
    }
    jobInstanceDo.setParentId(jobInstance.getParentId());
    jobInstanceDo.setJobId(jobInstance.getJobId());
    jobInstanceDo.setWorkerId(jobInstance.getWorkerId());
    jobInstanceDo.setTriggerType(jobInstance.getTriggerType());
    jobInstanceDo.setSchedulerInstance(jobInstance.getSchedulerInstance());
    jobInstanceDo.setExecutorHandler(jobInstance.getExecutorHandler());
    jobInstanceDo.setExecuteParam(jobInstance.getExecuteParam());
    jobInstanceDo.setExecutorInstance(jobInstance.getExecutorInstance());
    jobInstanceDo.setDispatchStatus(jobInstance.getDispatchStatus());
    jobInstanceDo.setDispatchMsg(jobInstance.getDispatchMsg());
    jobInstanceDo.setHandleTime(jobInstance.getHandleTime());
    jobInstanceDo.setFinishedTime(jobInstance.getFinishedTime());
    jobInstanceDo.setHandleStatus(jobInstance.getHandleStatus());
    jobInstanceDo.setResult(jobInstance.getResult());
    jobInstanceDo.setSequence(jobInstance.getSequence());
    jobInstanceDo.setCreatedTime(jobInstance.getCreatedTime());
    jobInstanceDo.setUpdateTime(jobInstance.getUpdateTime());
    return jobInstanceDo;
  }

  @Nonnull
  public static JobInstance toJobInstance(@Nonnull JobInstanceDo jobInstanceDo) {
    JobInstance jobInstance = new JobInstance();
    jobInstance.setInstanceId(jobInstanceDo.getInstanceId());
    jobInstance.setParentId(jobInstanceDo.getParentId());
    jobInstance.setJobId(jobInstanceDo.getJobId());
    jobInstance.setWorkerId(jobInstanceDo.getWorkerId());
    jobInstance.setTriggerType(jobInstanceDo.getTriggerType());
    jobInstance.setSchedulerInstance(jobInstanceDo.getSchedulerInstance());
    jobInstance.setExecutorHandler(jobInstanceDo.getExecutorHandler());
    jobInstance.setExecuteParam(jobInstanceDo.getExecuteParam());
    jobInstance.setExecutorInstance(jobInstanceDo.getExecutorInstance());
    jobInstance.setDispatchStatus(jobInstanceDo.getDispatchStatus());
    jobInstance.setDispatchMsg(jobInstanceDo.getDispatchMsg());
    jobInstance.setHandleTime(jobInstanceDo.getHandleTime());
    jobInstance.setFinishedTime(jobInstanceDo.getFinishedTime());
    jobInstance.setHandleStatus(jobInstanceDo.getHandleStatus());
    jobInstance.setResult(jobInstanceDo.getResult());
    jobInstance.setSequence(jobInstanceDo.getSequence());
    jobInstance.setCreatedTime(jobInstanceDo.getCreatedTime());
    jobInstance.setUpdateTime(jobInstanceDo.getUpdateTime());
    return jobInstance;
  }
}
