package com.zzsong.job.scheduler.core.admin.db.repository;

import com.zzsong.job.scheduler.core.admin.db.entity.JobExecutorDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
public interface JobExecutorRepository
        extends JpaRepository<JobExecutorDo, Long>,
        JpaSpecificationExecutor<JobExecutorDo> {

    @Nullable
    JobExecutorDo findTopByAppName(@Nonnull String appName);
}
