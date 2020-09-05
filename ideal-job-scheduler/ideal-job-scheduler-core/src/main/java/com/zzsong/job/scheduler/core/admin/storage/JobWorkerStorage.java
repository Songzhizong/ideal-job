package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.api.pojo.JobWorker;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
public interface JobWorkerStorage {
  Mono<Optional<JobWorker>> findById(long workerId);

  Mono<Optional<JobWorker>> findByAppName(@Nonnull String appName);

  Mono<JobWorker> save(@Nonnull JobWorker jobWorker);

  Mono<Integer> delete(long workerId);

  Mono<Res<List<ExecutorInfoRsp>>> query(QueryWorkerArgs args, Paging paging);
}
