package cn.sh.ideal.job.scheduler.core.converter;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.DBDefaults;
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum;
import cn.sh.ideal.job.scheduler.api.dto.req.CreateJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.rsp.JobInfoRsp;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
public final class JobInfoConverter {
  private static final ZoneOffset CHINA_ZONE_OFFSET = ZoneOffset.of("+8");

  @Nonnull
  public static JobInfo fromCreateJobArgs(@Nonnull CreateJobArgs createJobArgs) {
    JobInfo jobInfo = new JobInfo();
//    jobInfo.setJobId();
    final String application = createJobArgs.getApplication();
    if (StringUtils.isNotBlank(application)) {
      jobInfo.setApplication(application);
    } else {
      jobInfo.setApplication(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String tenantId = createJobArgs.getTenantId();
    if (StringUtils.isNotBlank(tenantId)) {
      jobInfo.setTenantId(tenantId);
    } else {
      jobInfo.setTenantId(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String bizType = createJobArgs.getBizType();
    if (StringUtils.isNotBlank(bizType)) {
      jobInfo.setBizType(bizType);
    } else {
      jobInfo.setBizType(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String customTag = createJobArgs.getCustomTag();
    if (StringUtils.isNotBlank(customTag)) {
      jobInfo.setCustomTag(customTag);
    } else {
      jobInfo.setCustomTag(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String businessId = createJobArgs.getBusinessId();
    if (StringUtils.isNotBlank(businessId)) {
      jobInfo.setBusinessId(businessId);
    } else {
      jobInfo.setBusinessId(DBDefaults.DEFAULT_STRING_VALUE);
    }
    jobInfo.setExecutorId(createJobArgs.getExecutorId());
    final String cron = createJobArgs.getCron();
    if (StringUtils.isNotBlank(cron)) {
      jobInfo.setCron(cron);
    } else {
      jobInfo.setCron(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String jobName = createJobArgs.getJobName();
    if (StringUtils.isNotBlank(jobName)) {
      jobInfo.setJobName(jobName);
    } else {
      jobInfo.setJobName(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String alarmEmail = createJobArgs.getAlarmEmail();
    if (StringUtils.isNotBlank(alarmEmail)) {
      jobInfo.setAlarmEmail(alarmEmail);
    } else {
      jobInfo.setAlarmEmail(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final LbStrategyEnum routeStrategy = createJobArgs.getRouteStrategy();
    if (routeStrategy != null) {
      jobInfo.setRouteStrategy(routeStrategy);
    } else {
      jobInfo.setRouteStrategy(LbStrategyEnum.POLLING);
    }
    final String executorHandler = createJobArgs.getExecutorHandler();
    if (executorHandler != null) {
      jobInfo.setExecutorHandler(executorHandler);
    } else {
      jobInfo.setExecutorHandler(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final String executorParam = createJobArgs.getExecutorParam();
    if (StringUtils.isNotBlank(executorParam)) {
      jobInfo.setExecutorParam(executorParam);
    } else {
      jobInfo.setExecutorParam(DBDefaults.DEFAULT_STRING_VALUE);
    }
    final BlockStrategyEnum blockStrategy = createJobArgs.getBlockStrategy();
    if (blockStrategy != null) {
      jobInfo.setBlockStrategy(blockStrategy);
    } else {
      jobInfo.setBlockStrategy(BlockStrategyEnum.SERIAL);
    }
    final Integer retryCount = createJobArgs.getRetryCount();
    if (retryCount == null || retryCount < 1) {
      jobInfo.setRetryCount(DBDefaults.DEFAULT_INT_VALUE);
    } else {
      jobInfo.setRetryCount(retryCount);
    }
    jobInfo.setJobStatus(JobInfo.JOB_STOP);
    jobInfo.setLastTriggerTime(0);
    jobInfo.setNextTriggerTime(0);
//    jobInfo.setCreatedTime();
//    jobInfo.setUpdateTime();
//    jobInfo.setDeleted();
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
    jobInfoRsp.setRouteStrategy(jobInfo.getRouteStrategy().getCode());
    jobInfoRsp.setExecutorHandler(jobInfo.getExecutorHandler());
    jobInfoRsp.setExecutorParam(jobInfo.getExecutorParam());
    jobInfoRsp.setBlockStrategy(jobInfo.getBlockStrategy().getCode());
    jobInfoRsp.setRetryCount(jobInfo.getRetryCount());
    jobInfoRsp.setChildJobId(jobInfo.getChildJobId());
    jobInfoRsp.setJobStatus(jobInfo.getJobStatus());
    long lastTriggerTime = jobInfo.getLastTriggerTime();
    if (lastTriggerTime > 0) {
      Instant instant = Instant.ofEpochMilli(lastTriggerTime);
      LocalDateTime dateTime = LocalDateTime.ofInstant(instant, CHINA_ZONE_OFFSET);
      jobInfoRsp.setLastTriggerTime(dateTime);
    }
    long nextTriggerTime = jobInfo.getNextTriggerTime();
    if (nextTriggerTime > 0) {
      Instant instant = Instant.ofEpochMilli(nextTriggerTime);
      LocalDateTime dateTime = LocalDateTime.ofInstant(instant, CHINA_ZONE_OFFSET);
      jobInfoRsp.setNextTriggerTime(dateTime);
    }
    jobInfoRsp.setCreatedTime(jobInfo.getCreatedTime());
    jobInfoRsp.setUpdateTime(jobInfo.getUpdateTime());
    return jobInfoRsp;
  }
}
