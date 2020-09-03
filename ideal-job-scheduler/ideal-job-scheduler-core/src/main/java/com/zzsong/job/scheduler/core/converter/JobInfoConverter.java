package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.common.constants.DBDefaults;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import com.zzsong.job.scheduler.core.admin.entity.JobInfo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
public final class JobInfoConverter {

    @Nonnull
    public static JobInfo fromCreateJobArgs(@Nonnull CreateJobArgs createJobArgs) {
        JobInfo jobInfo = new JobInfo();
//        jobInfo.setJobId();
        jobInfo.setExecutorId(createJobArgs.getExecutorId());
        jobInfo.setCron(createJobArgs.getCron());
        jobInfo.setJobName(createJobArgs.getJobName());
        jobInfo.setAlarmEmail(createJobArgs.getAlarmEmail());
        jobInfo.setRouteStrategy(createJobArgs.getRouteStrategy());
        jobInfo.setExecuteType(createJobArgs.getExecuteType());
        jobInfo.setExecutorHandler(createJobArgs.getExecutorHandler());
        jobInfo.setExecuteParam(createJobArgs.getExecuteParam());
        jobInfo.setBlockStrategy(createJobArgs.getBlockStrategy());
        jobInfo.setRetryCount(createJobArgs.getRetryCount());
        jobInfo.setJobStatus(JobInfo.JOB_STOP);
        jobInfo.setLastTriggerTime(DBDefaults.DEFAULT_LONG_VALUE);
        jobInfo.setNextTriggerTime(DBDefaults.DEFAULT_LONG_VALUE);
        jobInfo.setApplication(createJobArgs.getApplication());
        jobInfo.setTenantId(createJobArgs.getTenantId());
        jobInfo.setBizType(createJobArgs.getBizType());
        jobInfo.setCustomTag(createJobArgs.getCustomTag());
        jobInfo.setBusinessId(createJobArgs.getBusinessId());
//        jobInfo.setCreatedTime();
//        jobInfo.setUpdateTime();
//        jobInfo.setDeleted();
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
        jobInfoRsp.setExecutorId(jobInfo.getExecutorId());
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
            jobInfoRsp.setLastTriggerTime(DateTimes.parse(lastTriggerTime));
        }
        long nextTriggerTime = jobInfo.getNextTriggerTime();
        if (nextTriggerTime > 0) {
            jobInfoRsp.setNextTriggerTime(DateTimes.parse(nextTriggerTime));
        }
        jobInfoRsp.setCreatedTime(jobInfo.getCreatedTime());
        jobInfoRsp.setUpdateTime(jobInfo.getUpdateTime());
        return jobInfoRsp;
    }
}
