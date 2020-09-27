package com.zzsong.job.scheduler.core.admin.storage.converter;

import com.zzsong.job.scheduler.core.pojo.JobExecutor;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobExecutorDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/27
 */
public final class ExecutorDoConverter {

  @Nonnull
  public static JobExecutor toJobExecutor(@Nonnull JobExecutorDo executor) {
    JobExecutor jobExecutor = new JobExecutor();
    jobExecutor.setExecutorId(executor.getExecutorId());
    jobExecutor.setAppName(executor.getAppName());
    jobExecutor.setTitle(executor.getTitle());
    jobExecutor.setCreatedTime(executor.getCreatedTime());
    jobExecutor.setUpdateTime(executor.getUpdateTime());
    return jobExecutor;
  }

  @Nonnull
  public static JobExecutorDo fromJobExecutor(@Nonnull JobExecutor executor) {
    JobExecutorDo jobExecutorDo = new JobExecutorDo();
    //noinspection ConstantConditions
    if (executor.getExecutorId() != null && executor.getExecutorId() > 0) {
      jobExecutorDo.setExecutorId(executor.getExecutorId());
    }
    jobExecutorDo.setAppName(executor.getAppName());
    jobExecutorDo.setTitle(executor.getTitle());
    jobExecutorDo.setCreatedTime(executor.getCreatedTime());
    jobExecutorDo.setUpdateTime(executor.getUpdateTime());
    jobExecutorDo.setDeleted(0);
    return jobExecutorDo;
  }
}
