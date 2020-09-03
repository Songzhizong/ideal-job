package com.zzsong.job.scheduler.core.admin.repository;

import com.zzsong.job.scheduler.core.admin.entity.vo.DispatchJobView;

import java.util.Collection;

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
public interface JobInfoRepositoryCustom {
  void batchUpdateTriggerInfo(Collection<DispatchJobView> jobInfos);
}
