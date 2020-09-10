package com.zzsong.job.scheduler.core.admin.storage.db.jpa;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.core.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobWorkerDo;
import com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository.JobExecutorRepository;
import com.zzsong.job.scheduler.core.admin.storage.JobWorkerStorage;
import com.zzsong.job.scheduler.core.admin.storage.converter.WorkerDoConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/5
 */
@Component
public class JpaJobWorkerStorage implements JobWorkerStorage {
  private final Scheduler blockScheduler;
  private final JobExecutorRepository jobExecutorRepository;

  public JpaJobWorkerStorage(Scheduler blockScheduler,
                             JobExecutorRepository jobExecutorRepository) {
    this.blockScheduler = blockScheduler;
    this.jobExecutorRepository = jobExecutorRepository;
  }

  @Override
  public Mono<Optional<JobWorker>> findById(long workerId) {
    return Mono
        .just(
            jobExecutorRepository.findById(workerId)
                .map(WorkerDoConverter::toJobWorker)
        ).subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Optional<JobWorker>> findByAppName(@Nonnull String appName) {
    return Mono.just(jobExecutorRepository.findTopByAppName(appName)
        .map(WorkerDoConverter::toJobWorker))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<JobWorker> save(@Nonnull JobWorker jobWorker) {
    JobWorkerDo executorDo = WorkerDoConverter.fromJobWorker(jobWorker);
    return Mono.just(jobExecutorRepository.save(executorDo))
        .subscribeOn(blockScheduler)
        .map(WorkerDoConverter::toJobWorker);
  }

  @Override
  public Mono<Integer> delete(long workerId) {
    return Mono.just(jobExecutorRepository.deleteByWorkerId(workerId))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Res<List<JobWorker>>> query(QueryWorkerArgs args, Paging paging) {
    return Mono.just(1)
        .map(i -> {
          String appName = args.getAppName();
          String title = args.getTitle();
          Page<JobWorkerDo> page = jobExecutorRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(appName)) {
              predicates.add(cb.like(root.get("appName"), appName + "%"));
            }
            if (StringUtils.isNotBlank(title)) {
              predicates.add(cb.like(root.get("title"), title + "%"));
            }
            return cq.where(predicates.toArray(new Predicate[0])).getRestriction();
          }, SpringPages.paging2Pageable(paging));
          return SpringPages.toPageRes(page, WorkerDoConverter::toJobWorker);
        })
        .subscribeOn(blockScheduler);
  }
}
