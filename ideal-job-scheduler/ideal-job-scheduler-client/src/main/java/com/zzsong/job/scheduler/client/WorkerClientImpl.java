package com.zzsong.job.scheduler.client;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.WorkerClient;
import com.zzsong.job.scheduler.api.dto.req.CreateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobWorkerRsp;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/9
 */
public class WorkerClientImpl implements WorkerClient {
  @Nonnull
  @Override
  public Mono<Res<JobWorkerRsp>> create(@Nonnull CreateWorkerArgs args) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<JobWorkerRsp>> update(@Nonnull UpdateWorkerArgs args) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<Void>> delete(long workerId) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<List<JobWorkerRsp>>> query(@Nullable QueryWorkerArgs args,
                                             @Nullable Paging paging) {
    return null;
  }
}
