package cn.sh.ideal.nj.cmpt.job.common.executor;

import cn.sh.ideal.nj.cmpt.job.common.pojo.payload.ExecuteCallbackParam;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface RemoteExecutor extends Executor {
  /**
   * 任务执行结果回调
   */
  void callback(ExecuteCallbackParam param);
}
