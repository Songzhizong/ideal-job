package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import cn.sh.ideal.job.scheduler.core.admin.repository.JobTriggerLogRepository
import org.springframework.stereotype.Service

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Service
class JobTriggerLogService(private val jobTriggerLogRepository: JobTriggerLogRepository) {

  fun saveLog(log: JobTriggerLog): JobTriggerLog {
    return jobTriggerLogRepository.save(log)
  }

}