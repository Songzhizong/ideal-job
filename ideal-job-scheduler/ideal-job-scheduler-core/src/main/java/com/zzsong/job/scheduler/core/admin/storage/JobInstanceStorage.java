package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/6
 */
public interface JobInstanceStorage {

  Mono<JobInstance> save(@Nonnull JobInstance jobInstance);

  Mono<Optional<JobInstance>> findById(long instanceId);

  Mono<Integer> updateByTaskResult(@Nonnull TaskResult param);

  Mono<Integer> deleteAllByCreatedTimeLessThan(@Nonnull LocalDateTime time);
}
