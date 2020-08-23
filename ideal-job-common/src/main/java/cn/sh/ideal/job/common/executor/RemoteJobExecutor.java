package cn.sh.ideal.job.common.executor;

import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback;
import cn.sh.ideal.job.common.message.payload.IdleBeatCallback;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface RemoteJobExecutor extends JobExecutor {
  /**
   * 任务执行完成回调
   *
   * @param callback 回调消息
   * @author 宋志宗
   * @date 2020/8/22 23:47
   */
  void executeJobCallback(@Nonnull ExecuteJobCallback callback);

  /**
   * 空闲测试回调
   *
   * @param callback 回调消息
   * @author 宋志宗
   * @date 2020/8/22 23:47
   */
  void idleBeatCallback(@Nonnull IdleBeatCallback callback);
}