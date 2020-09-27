package com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository;

import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobExecutorDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/2
 */
public interface JobExecutorRepository
    extends JpaRepository<JobExecutorDo, Long>,
    JpaSpecificationExecutor<JobExecutorDo> {

  Optional<JobExecutorDo> findTopByAppName(@Nonnull String appName);

  @Modifying
  @Transactional(rollbackFor = Exception.class)
  @Query("update JobExecutorDo set deleted = 1 where executorId = :executorId")
  int deleteByExecutorId(@Param("executorId") long executorId);
}
