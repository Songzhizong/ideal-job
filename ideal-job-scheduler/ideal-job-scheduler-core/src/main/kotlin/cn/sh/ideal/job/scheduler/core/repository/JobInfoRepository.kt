package cn.sh.ideal.job.scheduler.core.repository

import cn.sh.ideal.job.scheduler.core.entity.JobInfo
import org.springframework.data.jpa.repository.JpaRepository

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
interface JobInfoRepository : JpaRepository<JobInfo, Long> {
}