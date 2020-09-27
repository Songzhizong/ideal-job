package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.scheduler.api.dto.rsp.JobExecutorRsp;
import com.zzsong.job.scheduler.core.pojo.JobExecutor;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/6
 */
public class JobExecutorConverter {
  @Nonnull
  public static JobExecutorRsp toJobExecutorRsp(@Nonnull JobExecutor jobExecutor) {
    JobExecutorRsp jobExecutorRsp = new JobExecutorRsp();
    jobExecutorRsp.setExecutorId(jobExecutor.getExecutorId());
    jobExecutorRsp.setAppName(jobExecutor.getAppName());
    jobExecutorRsp.setTitle(jobExecutor.getTitle());
    jobExecutorRsp.setCreatedTime(jobExecutor.getCreatedTime());
    jobExecutorRsp.setUpdateTime(jobExecutor.getUpdateTime());
    return jobExecutorRsp;
  }
}
