package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import cn.sh.ideal.job.scheduler.core.admin.repository.JobTriggerLogRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Service
class JobTriggerLogService(private val jobTriggerLogRepository: JobTriggerLogRepository) {

  fun saveLog(log: JobTriggerLog): JobTriggerLog {
    return jobTriggerLogRepository.save(log)
  }

  fun getLog(triggerId: Long): JobTriggerLog? {
    return jobTriggerLogRepository.findByIdOrNull(triggerId)
  }

  @Transactional(rollbackFor = [Exception::class])
  fun updateWhenTriggerCallback(triggerLog: JobTriggerLog): Int {
    return jobTriggerLogRepository.updateWhenTriggerCallback(triggerLog)
  }
}