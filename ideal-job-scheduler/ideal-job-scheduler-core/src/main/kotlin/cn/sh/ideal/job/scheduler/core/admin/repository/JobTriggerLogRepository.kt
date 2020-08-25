package cn.sh.ideal.job.scheduler.core.admin.repository

import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
interface JobTriggerLogRepository : JpaRepository<JobTriggerLog, Long> {

  @Query("""
    update JobTriggerLog log 
    set log.handleTime = :#{#triggerLog.handleTime},
        log.handleStatus = :#{#triggerLog.handleStatus},
        log.handleMsg = :#{#triggerLog.handleMsg},
        log.handleSequence = :#{#triggerLog.handleSequence},
        log.updateTime = :#{#triggerLog.updateTime}
    where log.triggerId = :#{#triggerLog.triggerId}
        and log.handleSequence < :#{#triggerLog.handleSequence}
  """)
  @Modifying
  fun updateWhenTriggerCallback(@Param("triggerLog") triggerLog: JobTriggerLog): Int
}