package cn.sh.ideal.job.scheduler.core.admin.repository.impl

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInfoRepositoryCustom
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
@Repository
class JobInfoRepositoryCustomImpl(private val jdbcTemplate: JdbcTemplate) : JobInfoRepositoryCustom {

  override fun batchUpdateTriggerInfo(jobInfos: Collection<JobInfo>) {
    val sql = """
      update job_info
      set job_status        = ?,
          last_trigger_time = ?,
          next_trigger_time = ?,
          update_time       = ?
      where job_id = ?
    """
    val now = LocalDateTime.now()
    val argsList = jobInfos.map {
      arrayOf(it.jobStatus, it.lastTriggerTime, it.nextTriggerTime, now, it.jobId)
    }
    jdbcTemplate.batchUpdate(sql, argsList)
  }
}