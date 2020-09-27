package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.dto.req.QueryExecutorArgs;
import com.zzsong.job.scheduler.core.pojo.JobExecutor;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/5
 */
public interface JobExecutorStorage {
  Mono<Optional<JobExecutor>> findById(long executorId);

  Mono<Optional<JobExecutor>> findByAppName(@Nonnull String appName);

  Mono<JobExecutor> save(@Nonnull JobExecutor jobExecutor);

  Mono<Integer> delete(long executorId);

  Mono<Res<List<JobExecutor>>> query(QueryExecutorArgs args, Paging paging);
}
