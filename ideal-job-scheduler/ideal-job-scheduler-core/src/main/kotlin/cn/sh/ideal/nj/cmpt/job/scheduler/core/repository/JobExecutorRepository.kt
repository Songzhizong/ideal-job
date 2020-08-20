package cn.sh.ideal.nj.cmpt.job.scheduler.core.repository

import cn.sh.ideal.nj.cmpt.job.scheduler.core.entity.JobExecutor
import org.springframework.data.jpa.repository.JpaRepository

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
interface JobExecutorRepository : JpaRepository<JobExecutor, Long> {
}