package com.zzsong.job.scheduler.api.client.impl;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.JobClient;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/9
 */
public class JobClientImpl implements JobClient {
  @Nonnull
  @Override
  public Mono<Res<JobInfoRsp>> create(@Nonnull CreateJobArgs createJobArgs) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<JobInfoRsp>> update(@Nonnull UpdateJobArgs updateJobArgs) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<Void>> remove(@Nonnull Long jobId) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<List<JobInfoRsp>>> query(@Nonnull QueryJobArgs args, @Nullable Paging paging) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<Void>> enable(@Nonnull Long jobId) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<Void>> disable(@Nonnull Long jobId) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Res<Void>> trigger(@Nonnull Long jobId, @Nullable String executeParam) {
    return null;
  }
}
