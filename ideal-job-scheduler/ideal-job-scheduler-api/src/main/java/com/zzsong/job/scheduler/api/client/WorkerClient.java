package com.zzsong.job.scheduler.api.client;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.dto.req.CreateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobWorkerRsp;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 执行器管理
 *
 * @author 宋志宗
 * @date 2020/8/26
 */
public interface WorkerClient {

  /**
   * 新增执行器
   *
   * @param args 新建参数
   * @return 执行器ID
   * @author 宋志宗
   * @date 2020/8/26 23:41
   */
  @Nonnull
  Mono<Res<JobWorkerRsp>> create(@Nonnull CreateWorkerArgs args);

  /**
   * 更新执行器信息
   *
   * @param args 更新参数
   * @return 更新结果
   * @author 宋志宗
   * @date 2020/8/26 23:42
   */
  @Nonnull
  Mono<Res<JobWorkerRsp>> update(@Nonnull UpdateWorkerArgs args);

  /**
   * 删除执行器
   *
   * @param workerId 执行器ID
   * @return 删除结果
   * @author 宋志宗
   * @date 2020/8/26 23:43
   */
  @Nonnull
  Mono<Res<Void>> delete(long workerId);

  /**
   * 查询执行器列表
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 执行器信息列表
   * @author 宋志宗
   * @date 2020/8/26 23:45
   */
  @Nonnull
  Mono<Res<List<JobWorkerRsp>>> query(@Nonnull QueryWorkerArgs args,
                                      @Nonnull Paging paging);
}
