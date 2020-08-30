package cn.sh.ideal.job.scheduler.core.admin.repository

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
interface JobInfoRepository : JpaRepository<JobInfo, Long>, JpaSpecificationExecutor<JobInfo>, JobInfoRepositoryCustom {

  @Query("""
    select new cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView(
        job.jobId,job.executorId,job.cron,job.routeStrategy,job.executeType,job.executorHandler,
        job.executeParam,job.blockStrategy,job.retryCount,job.jobStatus,job.lastTriggerTime,job.nextTriggerTime) 
    from JobInfo job
     where job.jobId = :jobId
  """)
  fun findDispatchJobViewById(jobId: Long): DispatchJobView?

  @Query("""
    select new cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView(
        job.jobId,job.executorId,job.cron,job.routeStrategy,job.executeType,job.executorHandler,
        job.executeParam,job.blockStrategy,job.retryCount,job.jobStatus,job.lastTriggerTime,job.nextTriggerTime) 
    from JobInfo job
    where job.jobStatus = :jobStatus and job.nextTriggerTime <= :maxNextTime
  """)
  fun loadScheduleJobViews(jobStatus: Int,
                           maxNextTime: Long,
                           pageable: Pageable): List<DispatchJobView>

  fun existsByExecutorId(executorId: Long): Boolean
}