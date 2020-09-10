package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.dto.req.QueryInstanceArgs;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/6
 */
public interface JobInstanceStorage {

  Mono<JobInstance> save(@Nonnull JobInstance jobInstance);

  Mono<Optional<JobInstance>> findById(long instanceId);

  Mono<Integer> updateByTaskResult(@Nonnull TaskResult param);

  Mono<Integer> deleteAllByCreatedTimeLessThan(@Nonnull LocalDateTime time);

  Mono<Res<List<JobInstance>>> query(@Nonnull QueryInstanceArgs args, @Nonnull Paging paging);
}
