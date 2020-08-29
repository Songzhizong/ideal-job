package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Service
class JobInstanceService(private val jobInstanceRepository: JobInstanceRepository) {


  fun saveInstance(instance: JobInstance): JobInstance {
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

  fun flush() {
    jobInstanceRepository.flush()
  }
}