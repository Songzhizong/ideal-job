package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.api.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.db.entity.JobExecutorDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
public final class ExecutorConverter {
  @Nonnull
  public static ExecutorInfoRsp toExecutorInfoRsp(@Nonnull JobExecutorDo executor) {
    ExecutorInfoRsp executorInfoRsp = new ExecutorInfoRsp();
    executorInfoRsp.setExecutorId(executor.getExecutorId());
    executorInfoRsp.setAppName(executor.getAppName());
    executorInfoRsp.setTitle(executor.getTitle());
    executorInfoRsp.setCreatedTime(executor.getCreatedTime());
    executorInfoRsp.setUpdateTime(executor.getUpdateTime());
    return executorInfoRsp;
  }

  @Nonnull
  public static JobWorker toJobWorker(@Nonnull JobExecutorDo executor) {
    JobWorker jobWorker = new JobWorker();
    jobWorker.setWorkerId(executor.getExecutorId());
    jobWorker.setAppName(executor.getAppName());
    jobWorker.setTitle(executor.getTitle());
    jobWorker.setCreatedTime(executor.getCreatedTime());
    jobWorker.setUpdateTime(executor.getUpdateTime());
    return jobWorker;
  }

  @Nonnull
  public static JobExecutorDo fromJobWorker(@Nonnull JobWorker worker) {
    JobExecutorDo jobExecutorDo = new JobExecutorDo();
    //noinspection ConstantConditions
    if (worker.getWorkerId() != null && worker.getWorkerId() > 0) {
      jobExecutorDo.setExecutorId(worker.getWorkerId());
    }
    jobExecutorDo.setAppName(worker.getAppName());
    jobExecutorDo.setTitle(worker.getTitle());
    jobExecutorDo.setCreatedTime(worker.getCreatedTime());
    jobExecutorDo.setUpdateTime(worker.getUpdateTime());
    jobExecutorDo.setDeleted(0);
    return jobExecutorDo;
  }
}
