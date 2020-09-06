package com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository;

import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInfoDo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
public interface JobInfoRepository
    extends JpaRepository<JobInfoDo, Long>,
    JpaSpecificationExecutor<JobInfoDo>, JobInfoRepositoryCustom {

  @Modifying
  @Transactional(rollbackFor = Exception.class)
  @Query("update JobInfoDo set deleted = 1 where jobId = :jobId")
  int deleteByJobId(@Param("jobId") long jobId);


  @Nullable
  @Query("select new com.zzsong.job.scheduler.core.pojo.JobView(" +
      "job.jobId, job.workerId, job.cron, job.routeStrategy, job.executeType, " +
      "job.executorHandler, job.executeParam, job.blockStrategy, job.retryCount, " +
      "job.jobStatus,job.lastTriggerTime,job.nextTriggerTime)" +
      " from JobInfoDo job" +
      " where job.jobId = :jobId")
  JobView findDispatchJobViewById(@Param("jobId") long jobId);

  @Nonnull
  @Query("select new com.zzsong.job.scheduler.core.pojo.JobView(" +
      "job.jobId, job.workerId, job.cron, job.routeStrategy, job.executeType, " +
      "job.executorHandler, job.executeParam, job.blockStrategy, job.retryCount, " +
      "job.jobStatus,job.lastTriggerTime,job.nextTriggerTime)" +
      " from JobInfoDo job " +
      " where job.jobStatus = :jobStatus and job.nextTriggerTime <= :maxNextTime")
  List<JobView> loadScheduleJobViews(@Param("jobStatus") int jobStatus,
                                     @Param("maxNextTime") long maxNextTime,
                                     @Param("pageable") Pageable pageable);

  boolean existsByWorkerId(@Param("workerId") long workerId);
}
