package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Service
class JobInstanceService(private val jobInstanceRepository: JobInstanceRepository) {
  private val maxResultLength = 10000


  fun saveInstance(instance: JobInstance): JobInstance {
    val result = instance.result
    if (result.length > maxResultLength) {
      instance.result = instance.result.substring(0, maxResultLength - 3).plus("...")
    }
    return jobInstanceRepository.save(instance)
  }

  fun getJobInstance(jobInstanceId: Long): JobInstance? {
    return jobInstanceRepository.findByIdOrNull(jobInstanceId)
  }


  fun updateDispatchInfo(instance: JobInstance) {
    jobInstanceRepository.updateDispatchInfo(instance)
  }

  fun updateWhenTriggerCallback(instance: JobInstance): Int {
    return jobInstanceRepository.updateWhenTriggerCallback(instance)
  }

  fun deleteAllByCreatedTimeLessThan(time: LocalDateTime): Int {
    return jobInstanceRepository.deleteAllByCreatedTimeLessThan(time)
  }

  fun flush() {
    jobInstanceRepository.flush()
  }
}