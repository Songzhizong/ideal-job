package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.core.admin.vo.JobInstanceVo;
import com.zzsong.job.scheduler.core.pojo.JobInstance;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/9/9
 */
public final class JobInstanceConverter {
  @Nonnull
  public static JobInstanceVo toJobInstanceVo(@Nonnull JobInstance instance) {
    JobInstanceVo jobInstanceVo = new JobInstanceVo();
    jobInstanceVo.setInstanceId(instance.getInstanceId());
    jobInstanceVo.setParentId(instance.getParentId());
    jobInstanceVo.setJobId(instance.getJobId());
    jobInstanceVo.setWorkerId(instance.getWorkerId());
    jobInstanceVo.setTriggerType(instance.getTriggerType().getDesc());
    jobInstanceVo.setSchedulerInstance(instance.getSchedulerInstance());
    jobInstanceVo.setExecutorHandler(instance.getExecutorHandler());
    jobInstanceVo.setExecuteParam(instance.getExecuteParam());
    jobInstanceVo.setCreatedTime(instance.getCreatedTime());
    jobInstanceVo.setUpdateTime(instance.getUpdateTime());
    jobInstanceVo.setExecutorInstance(instance.getExecutorInstance());
    jobInstanceVo.setDispatchStatus(instance.getDispatchStatus());
    jobInstanceVo.setDispatchMsg(instance.getDispatchMsg());

    final long handleTime = instance.getHandleTime();
    if (handleTime > 0) {
      final LocalDateTime parse = DateTimes.parse(handleTime);
      final String format = DateTimes.format(parse, DateTimes.yyyy_MM_dd_HH_mm_ss);
      jobInstanceVo.setHandleTime(format);
    } else {
      jobInstanceVo.setHandleTime("N/A");
    }

    final long finishedTime = instance.getFinishedTime();
    if (finishedTime > 0) {
      final LocalDateTime parse = DateTimes.parse(finishedTime);
      final String format = DateTimes.format(parse, DateTimes.yyyy_MM_dd_HH_mm_ss);
      jobInstanceVo.setFinishedTime(format);
    } else {
      jobInstanceVo.setFinishedTime("N/A");
    }

    jobInstanceVo.setHandleStatus(instance.getHandleStatus().getCode());
    jobInstanceVo.setResult(instance.getResult());
    return jobInstanceVo;

  }
}
