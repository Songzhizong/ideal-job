package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor
import cn.sh.ideal.job.scheduler.core.admin.repository.JobExecutorRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * @author 宋志宗
 * @date 2020/8/24
 */
@Service
class JobExecutorService(private val jobExecutorRepository: JobExecutorRepository) {

  fun loadById(executorId: Long): JobExecutor? {
    return jobExecutorRepository.findByIdOrNull(executorId)
  }

}