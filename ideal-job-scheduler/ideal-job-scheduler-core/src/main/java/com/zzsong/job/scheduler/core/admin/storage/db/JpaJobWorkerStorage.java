package com.zzsong.job.scheduler.core.admin.storage.db;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.api.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.db.entity.JobExecutorDo;
import com.zzsong.job.scheduler.core.admin.db.repository.JobExecutorRepository;
import com.zzsong.job.scheduler.core.admin.storage.JobWorkerStorage;
import com.zzsong.job.scheduler.core.converter.ExecutorConverter;
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
 * @author 宋志宗
 * @date 2020/9/5
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
    return Mono.just(jobExecutorRepository.findById(workerId)
        .map(ExecutorConverter::toJobWorker))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Optional<JobWorker>> findByAppName(@Nonnull String appName) {
    return Mono.just(jobExecutorRepository.findTopByAppName(appName)
        .map(ExecutorConverter::toJobWorker))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<JobWorker> save(@Nonnull JobWorker jobWorker) {
    JobExecutorDo executorDo = ExecutorConverter.fromJobWorker(jobWorker);
    return Mono.just(jobExecutorRepository.save(executorDo))
        .subscribeOn(blockScheduler)
        .map(ExecutorConverter::toJobWorker);
  }

  @Override
  public Mono<Integer> delete(long workerId) {
    return Mono.just(jobExecutorRepository.softDeleteByExecutorId(workerId))
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Res<List<ExecutorInfoRsp>>> query(QueryWorkerArgs args, Paging paging) {
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
          return SpringPages.toPageRes(page, ExecutorConverter::toExecutorInfoRsp);
        })
        .subscribeOn(blockScheduler);
  }
}
