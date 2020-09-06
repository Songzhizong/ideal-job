package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import com.zzsong.job.scheduler.core.pojo.JobInfo;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author 宋志宗
 * @date 2020/9/6
 */
@SuppressWarnings("DuplicatedCode")
public class JobInfoConverter {
  @Nonnull
  public static JobInfo fromCreateJobArgs(@Nonnull CreateJobArgs args) {
    JobInfo jobInfo = new JobInfo();
//    jobInfo.setJobId();
    jobInfo.setWorkerId(args.getWorkerId());
    jobInfo.setCron(args.getCron());
    jobInfo.setJobName(args.getJobName());
    jobInfo.setAlarmEmail(args.getAlarmEmail());
    jobInfo.setRouteStrategy(args.getRouteStrategy());
    jobInfo.setExecuteType(args.getExecuteType());
    jobInfo.setExecutorHandler(args.getExecutorHandler());
    jobInfo.setExecuteParam(args.getExecuteParam());
    jobInfo.setBlockStrategy(args.getBlockStrategy());
    jobInfo.setRetryCount(args.getRetryCount());
    jobInfo.setJobStatus(JobInfo.JOB_STOP);
    jobInfo.setLastTriggerTime(0);
//      jobInfo.setNextTriggerTime();
    jobInfo.setApplication(args.getApplication());
    jobInfo.setTenantId(args.getTenantId());
    jobInfo.setBizType(args.getBizType());
    jobInfo.setCustomTag(args.getCustomTag());
    jobInfo.setBusinessId(args.getBusinessId());
//      jobInfo.setCreatedTime();
//      jobInfo.setUpdateTime();
    return jobInfo;
  }

  @Nonnull
  public static JobInfoRsp toJobInfoRsp(@Nonnull JobInfo jobInfo) {
    JobInfoRsp jobInfoRsp = new JobInfoRsp();
    jobInfoRsp.setJobId(jobInfo.getJobId());
    jobInfoRsp.setApplication(jobInfo.getApplication());
    jobInfoRsp.setTenantId(jobInfo.getTenantId());
    jobInfoRsp.setBizType(jobInfo.getBizType());
    jobInfoRsp.setCustomTag(jobInfo.getCustomTag());
    jobInfoRsp.setBusinessId(jobInfo.getBusinessId());
    jobInfoRsp.setWorkerId(jobInfo.getWorkerId());
    jobInfoRsp.setCron(jobInfo.getCron());
    jobInfoRsp.setJobName(jobInfo.getJobName());
    jobInfoRsp.setAlarmEmail(jobInfo.getAlarmEmail());
    jobInfoRsp.setRouteStrategy(String.valueOf(jobInfo.getRouteStrategy()));
    jobInfoRsp.setExecutorHandler(jobInfo.getExecutorHandler());
    jobInfoRsp.setExecuteParam(jobInfo.getExecuteParam());
    jobInfoRsp.setBlockStrategy(String.valueOf(jobInfo.getBlockStrategy()));
    jobInfoRsp.setRetryCount(jobInfo.getRetryCount());
    jobInfoRsp.setJobStatus(jobInfo.getJobStatus());
    long lastTriggerTime = jobInfo.getLastTriggerTime();
    if (lastTriggerTime > 0) {
      LocalDateTime time = DateTimes.parse(lastTriggerTime);
      jobInfoRsp.setLastTriggerTime(time);
    }
    long nextTriggerTime = jobInfo.getNextTriggerTime();
    if (nextTriggerTime > 0) {
      LocalDateTime time = DateTimes.parse(nextTriggerTime);
      jobInfoRsp.setNextTriggerTime(time);
    }
    jobInfoRsp.setCreatedTime(jobInfo.getCreatedTime());
    jobInfoRsp.setUpdateTime(jobInfo.getUpdateTime());
    return jobInfoRsp;
  }
}
