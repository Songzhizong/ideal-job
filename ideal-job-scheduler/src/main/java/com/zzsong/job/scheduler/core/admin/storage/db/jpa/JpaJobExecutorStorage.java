package com.zzsong.job.scheduler.core.admin.storage.db.jpa;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.scheduler.api.dto.req.QueryExecutorArgs;
import com.zzsong.job.scheduler.core.pojo.JobExecutor;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobExecutorDo;
import com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository.JobExecutorRepository;
import com.zzsong.job.scheduler.core.admin.storage.JobExecutorStorage;
import com.zzsong.job.scheduler.core.admin.storage.converter.ExecutorDoConverter;
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
public class JpaJobExecutorStorage implements JobExecutorStorage {
  private final Scheduler blockScheduler;
  private final JobExecutorRepository jobExecutorRepository;

  public JpaJobExecutorStorage(Scheduler blockScheduler,
                               JobExecutorRepository jobExecutorRepository) {
    this.blockScheduler = blockScheduler;
    this.jobExecutorRepository = jobExecutorRepository;
  }

  @Override
  public Mono<Optional<JobExecutor>> findById(long executorId) {
    return Mono
        .just(
            jobExecutorRepository.findById(executorId)
                .map(ExecutorDoConverter::toJobExecutor)
        ).subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Optional<JobExecutor>> findByAppName(@Nonnull String appName) {
    return Mono.just(jobExecutorRepository.findTopByAppName(appName)
        .map(ExecutorDoConverter::toJobExecutor))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<JobExecutor> save(@Nonnull JobExecutor jobExecutor) {
    JobExecutorDo executorDo = ExecutorDoConverter.fromJobExecutor(jobExecutor);
    return Mono.just(jobExecutorRepository.save(executorDo))
        .subscribeOn(blockScheduler)
        .map(ExecutorDoConverter::toJobExecutor);
  }

  @Override
  public Mono<Integer> delete(long executorId) {
    return Mono.just(jobExecutorRepository.deleteByExecutorId(executorId))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Res<List<JobExecutor>>> query(QueryExecutorArgs args, Paging paging) {
    return Mono.just(1)
        .map(i -> {
          String appName = args.getAppName();
          String title = args.getTitle();
          Page<JobExecutorDo> page = jobExecutorRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(appName)) {
              predicates.add(cb.like(root.get("appName"), appName + "%"));
            }
            if (StringUtils.isNotBlank(title)) {
              predicates.add(cb.like(root.get("title"), title + "%"));
            }
            return cq.where(predicates.toArray(new Predicate[0])).getRestriction();
          }, SpringPages.paging2Pageable(paging));
          return SpringPages.toPageRes(page, ExecutorDoConverter::toJobExecutor);
        })
        .subscribeOn(blockScheduler);
  }
}
