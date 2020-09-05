package com.zzsong.job.scheduler.core.admin.db.repository;

import com.zzsong.job.scheduler.api.pojo.JobView;

import java.util.Collection;

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
public interface JobInfoRepositoryCustom {
  void batchUpdateTriggerInfo(Collection<JobView> jobInfos);
}
