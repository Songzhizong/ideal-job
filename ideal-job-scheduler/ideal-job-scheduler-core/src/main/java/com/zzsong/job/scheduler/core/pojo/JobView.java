package com.zzsong.job.scheduler.core.pojo;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.DBDefaults;
import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/30
 */
@SuppressWarnings("unused")
@Getter
@Setter
public class JobView {
  /**
   * 任务Id
   */
  private long jobId;
  /**
   * 任务名称
   */
  @Nonnull
  private String jobName = DBDefaults.DEFAULT_STRING_VALUE;
  /**
   * 所属执行器Id
   */
  private long workerId;
  /**
   * cron表达式
   */
  @Nonnull
  private String cron = "";
  /**
   * 执行器路由策略
   */
  @Nonnull
  private RouteStrategyEnum routeStrategy = RouteStrategyEnum.ROUND_ROBIN;
  /**
   * 执行模式
   */
  @Nonnull
  private ExecuteTypeEnum executeType = ExecuteTypeEnum.BEAN;
  /**
   * JobHandler
   */
  @Nonnull
  private String executorHandler = "";
  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam = "";
  /**
   * 阻塞处理策略
   */
  @Nonnull
  private BlockStrategyEnum blockStrategy = BlockStrategyEnum.PARALLEL;
  /**
   * 失败重试次数
   */
  private int retryCount;
  /**
   * 任务状态：0-停止，1-运行
   */
  private int jobStatus;
  /**
   * 上次调度时间
   */
  private long lastTriggerTime;
  /**
   * 下次调度时间
   */
  private long nextTriggerTime;

  public JobView() {
  }

  public JobView(long jobId, @Nonnull String jobName, long workerId, @Nonnull String cron,
                 @Nonnull RouteStrategyEnum routeStrategy,
                 @Nonnull ExecuteTypeEnum executeType, @Nonnull String executorHandler,
                 @Nonnull String executeParam, @Nonnull BlockStrategyEnum blockStrategy,
                 int retryCount, int jobStatus, long lastTriggerTime, long nextTriggerTime) {
    this.jobId = jobId;
    this.jobName = jobName;
    this.workerId = workerId;
    this.cron = cron;
    this.routeStrategy = routeStrategy;
    this.executeType = executeType;
    this.executorHandler = executorHandler;
    this.executeParam = executeParam;
    this.blockStrategy = blockStrategy;
    this.retryCount = retryCount;
    this.jobStatus = jobStatus;
    this.lastTriggerTime = lastTriggerTime;
    this.nextTriggerTime = nextTriggerTime;
  }
}
