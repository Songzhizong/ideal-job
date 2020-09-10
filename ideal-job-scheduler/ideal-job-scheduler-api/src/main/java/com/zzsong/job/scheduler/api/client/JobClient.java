package com.zzsong.job.scheduler.api.client;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 任务管理
 *
 * @author 宋志宗 on 2020/8/20
 */
public interface JobClient {

  /**
   * 新建任务
   *
   * @param createJobArgs 新增任务请求参数
   * @return 任务id
   * @author 宋志宗 on 2020/8/26 7:36 下午
   */
  @Nonnull
  Mono<Res<JobInfoRsp>> create(@Nonnull CreateJobArgs createJobArgs);

  /**
   * 更新任务信息
   *
   * @param updateJobArgs 更新参数
   * @return 更新结果
   * @author 宋志宗 on 2020/8/26 8:48 下午
   */
  @Nonnull
  Mono<Res<JobInfoRsp>> update(@Nonnull UpdateJobArgs updateJobArgs);

  /**
   * 移除任务
   *
   * @param jobId 任务id
   * @return 移除结果
   * @author 宋志宗 on 2020/8/26 8:49 下午
   */
  @Nonnull
  Mono<Res<Void>> remove(long jobId);

  /**
   * 查询任务信息
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 任务信息列表
   * @author 宋志宗 on 2020/8/26 8:51 下午
   */
  @Nonnull
  Mono<Res<List<JobInfoRsp>>> query(@Nonnull QueryJobArgs args,
                                    @Nullable Paging paging);

  /**
   * 启用任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗 on 2020/8/20 4:38 下午
   */
  @Nonnull
  Mono<Res<Void>> enable(long jobId);

  /**
   * 停用任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗 on 2020/8/20 4:38 下午
   */
  @Nonnull
  Mono<Res<Void>> disable(long jobId);

  /**
   * 触发任务
   *
   * @param jobId        任务id
   * @param executeParam 执行参数, 为<code>null</code>空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗 on 2020/8/20 4:18 下午
   */
  @Nonnull
  Mono<Res<Void>> trigger(long jobId, @Nullable String executeParam);
}
