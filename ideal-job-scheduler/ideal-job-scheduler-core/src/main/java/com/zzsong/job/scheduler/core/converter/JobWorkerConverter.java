package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.scheduler.api.dto.rsp.JobWorkerRsp;
import com.zzsong.job.scheduler.core.pojo.JobWorker;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/6
 */
public class JobWorkerConverter {
  @Nonnull
  public static JobWorkerRsp toJobWorkerRsp(@Nonnull JobWorker jobWorker) {
    JobWorkerRsp jobWorkerRsp = new JobWorkerRsp();
    jobWorkerRsp.setWorkerId(jobWorker.getWorkerId());
    jobWorkerRsp.setAppName(jobWorker.getAppName());
    jobWorkerRsp.setTitle(jobWorker.getTitle());
    jobWorkerRsp.setCreatedTime(jobWorker.getCreatedTime());
    jobWorkerRsp.setUpdateTime(jobWorker.getUpdateTime());
    return jobWorkerRsp;
  }
}
