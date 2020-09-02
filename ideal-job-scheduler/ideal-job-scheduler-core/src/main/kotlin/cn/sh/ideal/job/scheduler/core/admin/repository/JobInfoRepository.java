package cn.sh.ideal.job.scheduler.core.admin.repository;

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo;
import cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
public interface JobInfoRepository
        extends JpaRepository<JobInfo, Long>,
        JpaSpecificationExecutor<JobInfo>, JobInfoRepositoryCustom {


    @Nullable
    @Query("select new cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView(" +
            "job.jobId, job.executorId, job.cron, job.routeStrategy, job.executeType, " +
            "job.executorHandler, job.executeParam, job.blockStrategy, job.retryCount, " +
            "job.jobStatus,job.lastTriggerTime,job.nextTriggerTime)" +
            " from JobInfo job" +
            " where job.jobId = :jobId")
    DispatchJobView findDispatchJobViewById(long jobId);

    @Nonnull
    @Query("select new cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView(" +
            "job.jobId, job.executorId, job.cron, job.routeStrategy, job.executeType, " +
            "job.executorHandler, job.executeParam, job.blockStrategy, job.retryCount, " +
            "job.jobStatus,job.lastTriggerTime,job.nextTriggerTime)" +
            " from JobInfo job " +
            " where job.jobStatus = :jobStatus and job.nextTriggerTime <= :maxNextTime")
    List<DispatchJobView> loadScheduleJobViews(int jobStatus, long maxNextTime, Pageable pageable);

    boolean existsByExecutorId(long executorId);
}
