package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.core.pojo.JobInfo;
import com.zzsong.job.scheduler.core.pojo.JobView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/5
 */
public interface JobInfoStorage {
  /**
   * 执行器是否存在任务
   */
  Mono<Boolean> existsByExecutorId(long executorId);

  Mono<JobInfo> save(@Nonnull JobInfo jobInfo);

  Mono<Optional<JobInfo>> findById(long jobId);

  Mono<Integer> delete(long jobId);

  Mono<Optional<JobView>> findJobViewById(long jobId);

  Flux<JobView> loadScheduleJobViews(long maxNextTime, Paging paging);

  Mono<Integer> batchUpdateTriggerInfo(@Nonnull Collection<JobView> jobViewList);

  Mono<Res<List<JobInfo>>> query(@Nonnull QueryJobArgs args, @Nonnull Paging paging);
}
