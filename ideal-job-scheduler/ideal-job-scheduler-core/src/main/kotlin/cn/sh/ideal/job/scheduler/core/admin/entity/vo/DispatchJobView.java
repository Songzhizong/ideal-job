package cn.sh.ideal.job.scheduler.core.admin.entity.vo;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.ExecuteTypeEnum;
import cn.sh.ideal.job.common.constants.RouteStrategyEnum;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/30
 */
@SuppressWarnings("unused")
public class DispatchJobView {
  /**
   * 任务Id
   */
  private long jobId;
  /**
   * 所属执行器Id
   */
  private long executorId;
  @Nonnull
  private String cron = "";
  /**
   * 执行器路由策略
   */
  @Nonnull
  private RouteStrategyEnum routeStrategy = RouteStrategyEnum.POLLING;
  /**
   * 执行模式
   */
  @Nonnull
  private ExecuteTypeEnum executeType = ExecuteTypeEnum.JOB_HANDLER;
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

  public DispatchJobView() {
  }

  public DispatchJobView(long jobId, long executorId, @Nonnull String cron, @Nonnull RouteStrategyEnum routeStrategy,
                         @Nonnull ExecuteTypeEnum executeType, @Nonnull String executorHandler,
                         @Nonnull String executeParam, @Nonnull BlockStrategyEnum blockStrategy,
                         int retryCount, int jobStatus, long lastTriggerTime, long nextTriggerTime) {
    this.jobId = jobId;
    this.executorId = executorId;
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

  public long getJobId() {
    return jobId;
  }

  public void setJobId(long jobId) {
    this.jobId = jobId;
  }

  public long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(long executorId) {
    this.executorId = executorId;
  }

  @Nonnull
  public String getCron() {
    return cron;
  }

  public void setCron(@Nonnull String cron) {
    this.cron = cron;
  }

  @Nonnull
  public RouteStrategyEnum getRouteStrategy() {
    return routeStrategy;
  }

  public void setRouteStrategy(@Nonnull RouteStrategyEnum routeStrategy) {
    this.routeStrategy = routeStrategy;
  }

  @Nonnull
  public ExecuteTypeEnum getExecuteType() {
    return executeType;
  }

  public void setExecuteType(@Nonnull ExecuteTypeEnum executeType) {
    this.executeType = executeType;
  }

  @Nonnull
  public String getExecutorHandler() {
    return executorHandler;
  }

  public void setExecutorHandler(@Nonnull String executorHandler) {
    this.executorHandler = executorHandler;
  }

  @Nonnull
  public String getExecuteParam() {
    return executeParam;
  }

  public void setExecuteParam(@Nonnull String executeParam) {
    this.executeParam = executeParam;
  }

  @Nonnull
  public BlockStrategyEnum getBlockStrategy() {
    return blockStrategy;
  }

  public void setBlockStrategy(@Nonnull BlockStrategyEnum blockStrategy) {
    this.blockStrategy = blockStrategy;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public int getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(int jobStatus) {
    this.jobStatus = jobStatus;
  }

  public long getLastTriggerTime() {
    return lastTriggerTime;
  }

  public void setLastTriggerTime(long lastTriggerTime) {
    this.lastTriggerTime = lastTriggerTime;
  }

  public long getNextTriggerTime() {
    return nextTriggerTime;
  }

  public void setNextTriggerTime(long nextTriggerTime) {
    this.nextTriggerTime = nextTriggerTime;
  }
}
