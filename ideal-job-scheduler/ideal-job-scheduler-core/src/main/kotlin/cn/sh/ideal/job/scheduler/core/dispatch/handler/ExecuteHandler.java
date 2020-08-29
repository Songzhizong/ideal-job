package cn.sh.ideal.job.scheduler.core.dispatch.handler;

import cn.sh.ideal.job.common.constants.TriggerTypeEnum;
import cn.sh.ideal.job.common.transfer.Res;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
public interface ExecuteHandler {

  /**
   * 调度执行任务
   *
   * @param jobInfo             任务信息
   * @param triggerType         触发类型
   * @param customExecutorParam 自定义执行参数, 如果为空则使用任务默认配置
   * @author 宋志宗
   * @date 2020/8/28 10:23 下午
   */
  void execute(@Nonnull JobInstance instance,
               @Nonnull JobInfo jobInfo,
               @Nonnull TriggerTypeEnum triggerType,
               @Nullable String customExecutorParam);
}
