package cn.sh.ideal.job.common.executor;

import cn.sh.ideal.job.common.pojo.payload.ExecuteParam;
import cn.sh.ideal.job.common.loadbalancer.LbServer;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface Executor extends LbServer {

  /**
   * 执行任务
   *
   * @param param 触发器参数
   * @author 宋志宗
   * @date 2020/8/20 2:12 下午
   */
  boolean execute(ExecuteParam param);
}
