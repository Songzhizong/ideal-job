package com.zzsong.job.common.constants;

/**
 * @author 宋志宗 on 2020/9/10
 */
public interface WorkerRouter {
  /**
   * 客户端登录
   */
  String LOGIN = "worker-login";
  /**
   * 客户度执行任务
   */
  String EXECUTE = "worker-execute";
  /**
   * 任务执行结果回调
   */
  String TASK_CALLBACK = "worker-task-callback";
  /**
   * 任务执行日志上报
   */
  String INSTANCE_LOG = "worker-instance-log";
}
