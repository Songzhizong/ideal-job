package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.scheduler.api.pojo.JobWorker;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
public interface JobWorkerStorage {
    Mono<Optional<JobWorker>> findById(long id);

    Mono<Optional<JobWorker>> findByAppName(@Nonnull String appName);

    Mono<JobWorker> save(@Nonnull JobWorker jobWorker);

    Mono<Integer> delete(@Nonnull JobWorker jobWorker);
}
