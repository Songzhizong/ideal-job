package cn.sh.ideal.job.scheduler.core.admin.repository

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
interface JobInfoRepository : JpaRepository<JobInfo, Long>, JpaSpecificationExecutor<JobInfo>, JobInfoRepositoryCustom {
  fun existsByExecutorId(executorId: Long): Boolean

  fun findAllByJobStatusAndNextTriggerTimeLessThanEqual(jobStatus: Int,
                                                        maxNextTime: Long,
                                                        pageable: Pageable): List<JobInfo>
}