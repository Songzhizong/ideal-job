package com.zzsong.job.scheduler.core.admin.storage.db;

import com.zzsong.job.scheduler.api.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.storage.JobWorkerStorage;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
public class JpaJobWorkerStorage implements JobWorkerStorage {
    @Override
    public Mono<Optional<JobWorker>> findById(long id) {
        return null;
    }

    @Override
    public Mono<Optional<JobWorker>> findByAppName(@Nonnull String appName) {
        return null;
    }

    @Override
    public Mono<JobWorker> save(@Nonnull JobWorker jobWorker) {
        return null;
    }

    @Override
    public Mono<Integer> delete(@Nonnull JobWorker jobWorker) {
        return null;
    }
}
