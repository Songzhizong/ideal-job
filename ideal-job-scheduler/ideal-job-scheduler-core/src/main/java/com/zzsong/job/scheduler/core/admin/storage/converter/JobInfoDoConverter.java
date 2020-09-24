package com.zzsong.job.scheduler.core.admin.storage.converter;

import com.zzsong.job.scheduler.core.pojo.JobInfo;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInfoDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/26
 */
@SuppressWarnings("DuplicatedCode")
public final class JobInfoDoConverter {

  @Nonnull
  public static JobInfo toJobInfo(@Nonnull JobInfoDo jobInfoDo) {
    JobInfo jobInfo = new JobInfo();
    jobInfo.setJobId(jobInfoDo.getJobId());
    jobInfo.setWorkerId(jobInfoDo.getWorkerId());
    jobInfo.setCron(jobInfoDo.getCron());
    jobInfo.setJobName(jobInfoDo.getJobName());
    jobInfo.setDesc(jobInfoDo.getDesc());
    jobInfo.setAlarmEmail(jobInfoDo.getAlarmEmail());
    jobInfo.setRouteStrategy(jobInfoDo.getRouteStrategy());
    jobInfo.setExecuteType(jobInfoDo.getExecuteType());
    jobInfo.setExecutorHandler(jobInfoDo.getExecutorHandler());
    jobInfo.setExecuteParam(jobInfoDo.getExecuteParam());
    jobInfo.setBlockStrategy(jobInfoDo.getBlockStrategy());
    jobInfo.setRetryCount(jobInfoDo.getRetryCount());
    jobInfo.setJobStatus(jobInfoDo.getJobStatus());
    jobInfo.setLastTriggerTime(jobInfoDo.getLastTriggerTime());
    jobInfo.setNextTriggerTime(jobInfoDo.getNextTriggerTime());
    jobInfo.setApplication(jobInfoDo.getApplication());
    jobInfo.setTenantId(jobInfoDo.getTenantId());
    jobInfo.setBizType(jobInfoDo.getBizType());
    jobInfo.setCustomTag(jobInfoDo.getCustomTag());
    jobInfo.setBusinessId(jobInfoDo.getBusinessId());
    jobInfo.setCreatedTime(jobInfoDo.getCreatedTime());
    jobInfo.setUpdateTime(jobInfoDo.getUpdateTime());
    return jobInfo;
  }

  @Nonnull
  public static JobInfoDo fromJobInfo(@Nonnull JobInfo jobInfo) {
    JobInfoDo jobInfoDo = new JobInfoDo();
    //noinspection ConstantConditions
    if (jobInfo.getJobId() != null && jobInfo.getJobId() > 0) {
      jobInfoDo.setJobId(jobInfo.getJobId());
    }
    jobInfoDo.setWorkerId(jobInfo.getWorkerId());
    jobInfoDo.setCron(jobInfo.getCron());
    jobInfoDo.setJobName(jobInfo.getJobName());
    jobInfoDo.setDesc(jobInfo.getDesc());
    jobInfoDo.setAlarmEmail(jobInfo.getAlarmEmail());
    jobInfoDo.setRouteStrategy(jobInfo.getRouteStrategy());
    jobInfoDo.setExecuteType(jobInfo.getExecuteType());
    jobInfoDo.setExecutorHandler(jobInfo.getExecutorHandler());
    jobInfoDo.setExecuteParam(jobInfo.getExecuteParam());
    jobInfoDo.setBlockStrategy(jobInfo.getBlockStrategy());
    jobInfoDo.setRetryCount(jobInfo.getRetryCount());
    jobInfoDo.setJobStatus(jobInfo.getJobStatus());
    jobInfoDo.setLastTriggerTime(jobInfo.getLastTriggerTime());
    jobInfoDo.setNextTriggerTime(jobInfo.getNextTriggerTime());
    jobInfoDo.setApplication(jobInfo.getApplication());
    jobInfoDo.setTenantId(jobInfo.getTenantId());
    jobInfoDo.setBizType(jobInfo.getBizType());
    jobInfoDo.setCustomTag(jobInfo.getCustomTag());
    jobInfoDo.setBusinessId(jobInfo.getBusinessId());
    jobInfoDo.setCreatedTime(jobInfo.getCreatedTime());
    jobInfoDo.setUpdateTime(jobInfo.getUpdateTime());
//      jobInfoDo.setDeleted();
    return jobInfoDo;

  }
}
