package com.zzsong.job.scheduler.core.admin.storage.db.jpa;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.core.pojo.JobInfo;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInfoDo;
import com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository.JobInfoRepository;
import com.zzsong.job.scheduler.core.admin.storage.JobInfoStorage;
import com.zzsong.job.scheduler.core.admin.storage.converter.JobInfoDoConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/5
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
  public Mono<Boolean> existsByExecutorId(long executorId) {
    return Mono.just(executorId)
        .map(jobInfoRepository::existsByExecutorId)
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<JobInfo> save(@Nonnull JobInfo jobInfo) {
    JobInfoDo jobInfoDo = JobInfoDoConverter.fromJobInfo(jobInfo);
    return Mono.just(jobInfoDo)
        .map(jobInfoRepository::save)
        .subscribeOn(blockScheduler)
        .map(JobInfoDoConverter::toJobInfo);
  }

  @Override
  public Mono<Optional<JobInfo>> findById(long jobId) {
    return Mono.just(jobId)
        .map(id ->
            jobInfoRepository.findById(jobId)
                .map(JobInfoDoConverter::toJobInfo)
        )
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Integer> delete(long jobId) {
    return Mono.just(jobId)
        .map(jobInfoRepository::deleteByJobId)
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Optional<JobView>> findJobViewById(long jobId) {
    return Mono.just(jobId)
        .map(id -> Optional.ofNullable(jobInfoRepository.findDispatchJobViewById(id)))
        .subscribeOn(blockScheduler);

  }

  @Override
  public Flux<JobView> loadScheduleJobViews(long maxNextTime, Paging paging) {
    Pageable pageable = SpringPages.paging2Pageable(paging);
    return Flux.fromIterable(
        jobInfoRepository.loadScheduleJobViews(1, maxNextTime, pageable)
    ).subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Integer> batchUpdateTriggerInfo(@Nonnull Collection<JobView> jobViews) {
    return Mono.just(jobViews)
        .map(jobInfoRepository::batchUpdateTriggerInfo)
        .subscribeOn(blockScheduler);
  }

  @Override
  public Mono<Res<List<JobInfo>>> query(@Nonnull QueryJobArgs args, @Nonnull Paging paging) {
    return Mono.just(1)
        .map(i -> {
          Long executorId = args.getExecutorId();
          String jobName = args.getJobName();
          String executorHandler = args.getExecutorHandler();
          Integer jobStatus = args.getJobStatus();
          String application = args.getApplication();
          String tenantId = args.getTenantId();
          String bizType = args.getBizType();
          String customTag = args.getCustomTag();
          String businessId = args.getBusinessId();
          Page<JobInfoDo> page = jobInfoRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (executorId != null) {
              predicates.add(cb.equal(root.get("executorId"), executorId));
            }
            if (StringUtils.isNotBlank(jobName)) {
              predicates.add(cb.like(root.get("jobName"), jobName + "%"));
            }
            if (StringUtils.isNotBlank(executorHandler)) {
              predicates.add(cb.like(root.get("executorHandler"), executorHandler + "%"));
            }
            if (jobStatus != null) {
              predicates.add(cb.equal(root.get("jobStatus"), jobStatus));
            }
            if (StringUtils.isNotBlank(application)) {
              predicates.add(cb.equal(root.get("application"), application));
            }
            if (StringUtils.isNotBlank(tenantId)) {
              predicates.add(cb.equal(root.get("tenantId"), tenantId));
            }
            if (StringUtils.isNotBlank(bizType)) {
              predicates.add(cb.equal(root.get("bizType"), bizType));
            }
            if (StringUtils.isNotBlank(customTag)) {
              predicates.add(cb.equal(root.get("customTag"), customTag));
            }
            if (StringUtils.isNotBlank(businessId)) {
              predicates.add(cb.equal(root.get("businessId"), businessId));
            }
            return cq.where(predicates.toArray(new Predicate[0])).getRestriction();
          }, SpringPages.paging2Pageable(paging));
          return SpringPages.toPageRes(page, JobInfoDoConverter::toJobInfo);
        }).subscribeOn(blockScheduler);
  }
}
