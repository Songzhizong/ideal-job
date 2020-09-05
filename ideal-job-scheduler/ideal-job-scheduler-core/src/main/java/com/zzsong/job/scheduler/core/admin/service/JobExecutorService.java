package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.common.cache.ReactiveCache;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.scheduler.api.dto.req.CreateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.core.admin.db.entity.JobExecutorDo;
import com.zzsong.job.scheduler.core.admin.db.repository.JobExecutorRepository;
import com.zzsong.job.scheduler.api.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.storage.JobInfoStorage;
import com.zzsong.job.scheduler.core.admin.storage.JobWorkerStorage;
import com.zzsong.job.scheduler.core.converter.ExecutorConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@Service
public class JobExecutorService {
    private static final Logger log = LoggerFactory.getLogger(JobExecutorService.class);
    private static final String CACHE_NAME = "ideal:job:cache:worker:";
    private static final Duration CACHE_EXPIRE = Duration.ofDays(1);

    private final ReactiveCache reactiveCache;
    private final JobInfoStorage jobInfoStorage;
    private final JobWorkerStorage jobWorkerStorage;
    private final JobExecutorRepository jobExecutorRepository;

    public JobExecutorService(ReactiveCache reactiveCache,
                              JobInfoStorage jobInfoStorage,
                              JobWorkerStorage jobWorkerStorage,
                              JobExecutorRepository jobExecutorRepository) {
        this.reactiveCache = reactiveCache;
        this.jobInfoStorage = jobInfoStorage;
        this.jobWorkerStorage = jobWorkerStorage;
        this.jobExecutorRepository = jobExecutorRepository;
    }

    public Mono<Long> create(@Nonnull CreateExecutorArgs args) {
        String appName = args.getAppName();
        String title = args.getTitle();
        return jobWorkerStorage.findByAppName(appName)
                .flatMap(op -> {
                    if (op.isPresent()) {
                        log.info("appName: {} 已存在", appName);
                        return Mono.error(new VisibleException("appName已存在"));
                    } else {
                        LocalDateTime now = DateTimes.now();
                        JobWorker worker = new JobWorker();
                        worker.setAppName(appName);
                        worker.setTitle(title);
                        worker.setCreatedTime(now);
                        worker.setUpdateTime(now);
                        return jobWorkerStorage.save(worker)
                                .flatMap(savedWorker -> {
                                    long workerId = savedWorker.getWorkerId();
                                    String key = CACHE_NAME + workerId;
                                    String value = JsonUtils.toJsonString(savedWorker);
                                    return reactiveCache.set(key, value, CACHE_EXPIRE)
                                            .map(b -> workerId);
                                });
                    }
                });
    }

    @Nonnull
    public Mono<JobWorker> update(@Nonnull UpdateExecutorArgs updateArgs) {
        Long executorId = updateArgs.getExecutorId();
        String appName = updateArgs.getAppName();
        String title = updateArgs.getTitle();
        Mono<Optional<JobWorker>> byAppName = jobWorkerStorage.findByAppName(appName);
        Mono<Optional<JobWorker>> byId = jobWorkerStorage.findById(executorId);
        return Mono.zip(byAppName, byId)
                .flatMap(t -> {
                    Optional<Long> byAppNameId = t.getT1().map(JobWorker::getWorkerId);
                    Optional<JobWorker> jobWorkerOptional = t.getT2();
                    Long a;
                    if (byAppNameId.isPresent() && (a = byAppNameId.get()).equals(executorId)) {
                        log.info("appName: {} 已被: {} 使用", appName, a);
                        return Mono.error(new VisibleException("appName已被使用"));
                    }
                    if (!jobWorkerOptional.isPresent()) {
                        log.info("执行器: {} 不存在", executorId);
                        return Mono.error(new VisibleException("执行器不存在"));
                    }
                    LocalDateTime now = LocalDateTime.now();
                    JobWorker worker = jobWorkerOptional.get();
                    worker.setAppName(appName);
                    worker.setTitle(title);
                    worker.setUpdateTime(now);
                    return jobWorkerStorage.save(worker)
                            .flatMap(savedWorker -> {
                                long workerId = savedWorker.getWorkerId();
                                String key = CACHE_NAME + workerId;
                                String value = JsonUtils.toJsonString(savedWorker);
                                return reactiveCache.set(key, value, CACHE_EXPIRE)
                                        .map(b -> worker);
                            });
                });
    }

    public Mono<Integer> delete(long workerId) {
        return jobInfoStorage.existsByWorkerId(workerId)
                .flatMap(exist -> {
                    if (exist) {
                        // 执行器存在定时任务则无法删除
                        return Mono.error(new VisibleException("该执行器存在定时任务"));
                    } else {
                        return jobWorkerStorage.findById(workerId)
                                .flatMap(op -> {
                                    // 执行器不存在, 直接返回结果
                                    if (!op.isPresent()) {
                                        log.info("执行器: {} 不存在", workerId);
                                        return Mono.just(0);
                                    }
                                    // 执行器存在, 删除执行器, 删除缓存
                                    JobWorker jobWorker = op.get();
                                    return jobWorkerStorage.delete(jobWorker)
                                            .flatMap(count -> {
                                                String key = CACHE_NAME + workerId;
                                                return reactiveCache.delete(key)
                                                        .map(b -> count);
                                            });
                                });
                    }
                });
    }

    @Nonnull
    public Res<List<ExecutorInfoRsp>> query(@Nonnull QueryExecutorArgs args,
                                            @Nonnull Paging paging) {
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
    }

    @Nonnull
    public Mono<Optional<JobWorker>> loadById(long workerId) {
        String key = CACHE_NAME + workerId;
        return reactiveCache.get(key).defaultIfEmpty("")
                .flatMap(cache -> {
                    // 如果缓存中存在, 直接返回缓存的内衣
                    if (StringUtils.isNotBlank(cache)) {
                        JobWorker worker = JsonUtils.parseJson(cache, JobWorker.class);
                        return Mono.just(Optional.of(worker));
                    } else {
                        // 缓存不存在, 尝试从库中获取
                        return jobWorkerStorage.findById(workerId)
                                .flatMap(load -> {
                                    if (!load.isPresent()) {
                                        // 如果库中也不存在, 直接返回empty
                                        return Mono.just(Optional.empty());
                                    } else {
                                        // 库中存在, 刷入缓存, 返回执行器信息
                                        JobWorker worker = load.get();
                                        String value = JsonUtils.toJsonString(worker);
                                        return reactiveCache.set(key, value, CACHE_EXPIRE)
                                                .map(b -> Optional.of(worker));
                                    }
                                });

                    }
                });

    }
}
