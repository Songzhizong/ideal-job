package cn.sh.ideal.job.scheduler.core.admin.repository;

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo;

import java.util.Collection;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
public interface JobInfoRepositoryCustom {
  void batchUpdate(Collection<JobInfo> jobInfos);
}
