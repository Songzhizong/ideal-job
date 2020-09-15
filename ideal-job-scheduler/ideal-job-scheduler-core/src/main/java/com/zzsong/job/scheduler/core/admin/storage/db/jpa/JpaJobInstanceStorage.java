package com.zzsong.job.scheduler.core.admin.storage.db.jpa;

import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.transfer.*;
import com.zzsong.job.scheduler.api.dto.req.QueryInstanceArgs;
import com.zzsong.job.scheduler.core.admin.storage.JobInstanceStorage;
import com.zzsong.job.scheduler.core.admin.storage.converter.JobInstanceDoConverter;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInstanceDo;
import com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository.JobInstanceRepository;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/6
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
        .map(repository::updateByTaskResult)
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Integer> deleteAllByCreatedTimeLessThan(@Nonnull LocalDateTime time) {
    return Mono.just(time)
        .map(repository::deleteAllByCreatedTimeLessThan)
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Res<List<JobInstance>>> query(@Nonnull QueryInstanceArgs args,
                                            @Nonnull Paging paging) {
    final Long jobId = args.getJobId();
    final Long parentId = args.getParentId();
    final Long workerId = args.getWorkerId();
    final HandleStatusEnum handleStatus = args.getHandleStatus();
    final Range<LocalDateTime> range = args.getDispatchTimeRange();
    return Mono.just(1)
        .map(i -> {
          final Page<JobInstanceDo> page = repository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (parentId != null && parentId > 0) {
              predicates.add(cb.equal(root.get("parentId"), parentId));
            }
            if (jobId != null) {
              predicates.add(cb.equal(root.get("jobId"), jobId));
            }
            if (workerId != null && workerId > 0) {
              predicates.add(cb.equal(root.get("workerId"), workerId));
            }
            if (handleStatus != null) {
              predicates.add(cb.equal(root.get("handleStatus"), handleStatus));
            }
            if (range != null) {
              final LocalDateTime start = range.getStart();
              if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), start));
              }
              final LocalDateTime end = range.getEnd();
              if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), end));
              }
            }
            return cq.where(predicates.toArray(new Predicate[0])).getRestriction();
          }, SpringPages.paging2Pageable(paging));
          return SpringPages.toPageRes(page, JobInstanceDoConverter::toJobInstance);
        }).subscribeOn(blockScheduler);
  }
}
