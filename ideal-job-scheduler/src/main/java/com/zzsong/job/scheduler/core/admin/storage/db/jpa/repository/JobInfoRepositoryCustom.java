package com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository;

import com.zzsong.job.scheduler.core.pojo.JobView;

import java.util.Collection;

/**
 * @author 宋志宗 on 2020/8/27
 */
public interface JobInfoRepositoryCustom {
  int batchUpdateTriggerInfo(Collection<JobView> jobViewList);
}
