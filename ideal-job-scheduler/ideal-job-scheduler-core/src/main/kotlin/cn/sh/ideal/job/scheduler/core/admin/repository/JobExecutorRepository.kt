package cn.sh.ideal.job.scheduler.core.admin.repository

import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
interface JobExecutorRepository : JpaRepository<JobExecutor, Long>, JpaSpecificationExecutor<JobExecutor> {

  fun findTopByAppName(appName: String): JobExecutor?
}