package com.zzsong.job.scheduler.core.admin.db.repository;

import com.zzsong.job.scheduler.api.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.db.entity.JobInfoDo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
public interface JobInfoRepository
        extends JpaRepository<JobInfoDo, Long>,
        JpaSpecificationExecutor<JobInfoDo>, JobInfoRepositoryCustom {


    @Nullable
    @Query("select new com.zzsong.job.scheduler.api.pojo.JobView(" +
            "job.jobId, job.executorId, job.cron, job.routeStrategy, job.executeType, " +
            "job.executorHandler, job.executeParam, job.blockStrategy, job.retryCount, " +
            "job.jobStatus,job.lastTriggerTime,job.nextTriggerTime)" +
            " from JobInfoDo job" +
            " where job.jobId = :jobId")
    JobView findDispatchJobViewById(@Param("jobId") long jobId);

    @Nonnull
    @Query("select new com.zzsong.job.scheduler.api.pojo.JobView(" +
            "job.jobId, job.executorId, job.cron, job.routeStrategy, job.executeType, " +
            "job.executorHandler, job.executeParam, job.blockStrategy, job.retryCount, " +
            "job.jobStatus,job.lastTriggerTime,job.nextTriggerTime)" +
            " from JobInfoDo job " +
            " where job.jobStatus = :jobStatus and job.nextTriggerTime <= :maxNextTime")
    List<JobView> loadScheduleJobViews(@Param("jobStatus") int jobStatus,
                                       @Param("maxNextTime") long maxNextTime,
                                       @Param("pageable") Pageable pageable);

    boolean existsByExecutorId(@Param("executorId") long executorId);
}
