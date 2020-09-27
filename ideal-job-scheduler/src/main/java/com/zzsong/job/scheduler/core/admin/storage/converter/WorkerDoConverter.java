package com.zzsong.job.scheduler.core.admin.storage.converter;

import com.zzsong.job.scheduler.core.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobWorkerDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/27
 */
public final class WorkerDoConverter {

  @Nonnull
  public static JobWorker toJobWorker(@Nonnull JobWorkerDo executor) {
    JobWorker jobWorker = new JobWorker();
    jobWorker.setWorkerId(executor.getWorkerId());
    jobWorker.setAppName(executor.getAppName());
    jobWorker.setTitle(executor.getTitle());
    jobWorker.setCreatedTime(executor.getCreatedTime());
    jobWorker.setUpdateTime(executor.getUpdateTime());
    return jobWorker;
  }

  @Nonnull
  public static JobWorkerDo fromJobWorker(@Nonnull JobWorker worker) {
    JobWorkerDo jobWorkerDo = new JobWorkerDo();
    //noinspection ConstantConditions
    if (worker.getWorkerId() != null && worker.getWorkerId() > 0) {
      jobWorkerDo.setWorkerId(worker.getWorkerId());
    }
    jobWorkerDo.setAppName(worker.getAppName());
    jobWorkerDo.setTitle(worker.getTitle());
    jobWorkerDo.setCreatedTime(worker.getCreatedTime());
    jobWorkerDo.setUpdateTime(worker.getUpdateTime());
    jobWorkerDo.setDeleted(0);
    return jobWorkerDo;
  }
}
