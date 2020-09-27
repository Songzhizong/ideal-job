package com.zzsong.job.scheduler.core.admin.storage.db.jpa;

import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.core.admin.storage.InstanceLogStorage;
import com.zzsong.job.scheduler.core.admin.storage.converter.InstanceLogDoConverter;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.InstanceLogDo;
import com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository.InstanceLogRepository;
import com.zzsong.job.scheduler.core.pojo.InstanceLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/15
 */
@Component
public class JpaInstanceLogStorage implements InstanceLogStorage {
  private final Scheduler blockScheduler;
  private final InstanceLogRepository repository;

  public JpaInstanceLogStorage(Scheduler blockScheduler,
                               InstanceLogRepository repository) {
    this.blockScheduler = blockScheduler;
    this.repository = repository;
  }

  @Override
  public Mono<InstanceLog> save(@Nonnull InstanceLog instanceLog) {
    InstanceLogDo instanceLogDo = InstanceLogDoConverter.fromInstanceLog(instanceLog);
    return Mono.just(instanceLogDo)
        .map(repository::save)
        .subscribeOn(blockScheduler)
        .map(InstanceLogDoConverter::toInstanceLog);
  }

  @Override
  public Mono<List<InstanceLog>> loadByInstanceId(long instanceId) {
    return Mono.just(instanceId)
        .map(id ->
            repository.findAllByInstanceId(id)
                .stream()
                .map(InstanceLogDoConverter::toInstanceLog)
                .collect(Collectors.toList())
        )
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Integer> deleteAllByLogTimeLessThan(@Nonnull LocalDateTime time) {
    long timestamp = DateTimes.getTimestamp(time);
    return Mono.just(timestamp)
        .map(repository::deleteAllByLogTimeLessThan)
        .subscribeOn(blockScheduler);
  }
}
