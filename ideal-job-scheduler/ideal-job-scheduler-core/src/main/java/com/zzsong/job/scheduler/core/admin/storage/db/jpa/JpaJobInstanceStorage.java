package com.zzsong.job.scheduler.core.admin.storage.db.jpa;

import com.zzsong.job.scheduler.core.admin.storage.JobInstanceStorage;
import com.zzsong.job.scheduler.core.admin.storage.converter.JobInstanceDoConverter;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInstanceDo;
import com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository.JobInstanceRepository;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/6
 */
@Component
public class JpaJobInstanceStorage implements JobInstanceStorage {
  private final Scheduler blockScheduler;
  private final JobInstanceRepository repository;

  public JpaJobInstanceStorage(Scheduler blockScheduler,
                               JobInstanceRepository repository) {
    this.blockScheduler = blockScheduler;
    this.repository = repository;
  }

  @Override
  public Mono<JobInstance> save(@Nonnull JobInstance jobInstance) {
    JobInstanceDo jobInstanceDo = JobInstanceDoConverter.fromJobInstance(jobInstance);
    return Mono.just(jobInstanceDo)
        .map(repository::save)
        .subscribeOn(blockScheduler)
        .map(JobInstanceDoConverter::toJobInstance);
  }

  @Override
  public Mono<Optional<JobInstance>> findById(long instanceId) {
    return Mono.just(instanceId)
        .map(id ->
            repository.findById(id)
                .map(JobInstanceDoConverter::toJobInstance)
        )
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Integer> updateByTaskResult(@Nonnull TaskResult param) {
    return Mono.just(param)
        .map(repository::updateWhenTriggerCallback)
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Integer> deleteAllByCreatedTimeLessThan(@Nonnull LocalDateTime time) {
    return Mono.just(time)
        .map(repository::deleteAllByCreatedTimeLessThan)
        .subscribeOn(blockScheduler);
  }
}
