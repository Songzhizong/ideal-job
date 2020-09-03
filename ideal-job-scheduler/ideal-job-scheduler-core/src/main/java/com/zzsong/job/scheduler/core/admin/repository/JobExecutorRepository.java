package com.zzsong.job.scheduler.core.admin.repository;

import com.zzsong.job.scheduler.core.admin.entity.JobExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
public interface JobExecutorRepository
        extends JpaRepository<JobExecutor, Long>,
        JpaSpecificationExecutor<JobExecutor> {

    @Nullable
    JobExecutor findTopByAppName(@Nonnull String appName);
}
