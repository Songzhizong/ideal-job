package cn.sh.ideal.nj.cmpt.job.scheduler.api.client;

import cn.sh.ideal.nj.cmpt.job.common.res.Res;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface JobApi {

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
   * @param executorParam 执行参数
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:18 下午
   */
  @Nonnull
  Res<Void> trigger(@NotNull(message = "任务id不能为空")
                    @Nonnull Long jobId,
                    @Nullable String executorParam);
}
