package cn.sh.ideal.job.scheduler.core.admin.repository

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
@Repository
class JobInfoRepositoryCustomImpl : JobInfoRepositoryCustom {

  override fun batchUpdate(jobInfos: Collection<JobInfo>) {
    TODO("Not yet implemented")
  }
}