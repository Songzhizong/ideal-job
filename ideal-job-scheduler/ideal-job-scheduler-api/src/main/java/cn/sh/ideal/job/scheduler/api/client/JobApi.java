package cn.sh.ideal.job.scheduler.api.client;

import cn.sh.ideal.job.common.transfer.Paging;
import cn.sh.ideal.job.common.transfer.Res;
import cn.sh.ideal.job.scheduler.api.dto.req.CreateJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.QueryJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.rsp.JobInfoRsp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface JobApi {

  /**
   * 新建任务
   *
   * @param createJobArgs 新增任务请求参数
   * @return 任务id
   * @author 宋志宗
   * @date 2020/8/26 7:36 下午
   */
  @Nonnull
  Res<Long> create(@Nonnull CreateJobArgs createJobArgs);

  /**
   * 更新任务信息
   *
   * @param updateJobArgs 更新参数
   * @return 更新结果
   * @author 宋志宗
   * @date 2020/8/26 8:48 下午
   */
  @Nonnull
  Res<Void> update(@Nonnull UpdateJobArgs updateJobArgs);

  /**
   * 移除任务
   *
   * @param jobId 任务id
   * @return 移除结果
   * @author 宋志宗
   * @date 2020/8/26 8:49 下午
   */
  @Nonnull
  Res<Void> remove(@NotNull(message = "任务id不能为空") @Nonnull Long jobId);

  /**
   * 查询任务信息
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 任务信息列表
   * @author 宋志宗
   * @date 2020/8/26 8:51 下午
   */
  @Nonnull
  Res<List<JobInfoRsp>> query(@Nullable QueryJobArgs args,
                              @Nullable Paging paging);

  /**
   * 启动任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  @Nonnull
  Res<Void> start(@NotNull(message = "任务id不能为空") @Nonnull Long jobId);

  /**
   * 停止任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  @Nonnull
  Res<Void> stop(@NotNull(message = "任务id不能为空") @Nonnull Long jobId);

  /**
   * 触发任务
   *
   * @param jobId         任务id
   * @param executorParam 执行参数, 为<code>null</code>空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:18 下午
   */
  @Nonnull
  Res<Void> trigger(@NotNull(message = "任务id不能为空")
                    @Nonnull Long jobId,
                    @Nullable String executorParam);
}
