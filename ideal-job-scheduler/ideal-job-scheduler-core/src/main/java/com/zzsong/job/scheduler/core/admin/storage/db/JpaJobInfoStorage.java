package com.zzsong.job.scheduler.core.admin.storage.db;

import com.zzsong.job.scheduler.core.admin.db.repository.JobInfoRepository;
import com.zzsong.job.scheduler.core.admin.storage.JobInfoStorage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
@Component
public class JpaJobInfoStorage implements JobInfoStorage {
  private final Scheduler blockScheduler;
  private final JobInfoRepository jobInfoRepository;

  public JpaJobInfoStorage(Scheduler blockScheduler,
                           JobInfoRepository jobInfoRepository) {
    this.blockScheduler = blockScheduler;
    this.jobInfoRepository = jobInfoRepository;
  }

  @Override
  public Mono<Boolean> existsByWorkerId(long workerId) {
    return Mono.just(workerId).publishOn(blockScheduler)
        .map(jobInfoRepository::existsByExecutorId);
  }
}
