package cn.sh.ideal.job.common.executor;

import cn.sh.ideal.job.common.message.payload.ExecuteJobParam;
import cn.sh.ideal.job.common.loadbalancer.LbServer;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface JobExecutor extends LbServer {

  /**
   * 执行任务
   *
   * @param param 触发器参数
   * @author 宋志宗
   * @date 2020/8/20 2:12 下午
   */
  void executeJob(@Nonnull ExecuteJobParam param);
}
