package cn.sh.ideal.job.scheduler.core.dispatch;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.TriggerTypeEnum;
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public class TriggerParam {
  /**
   * 任务Id
   */
  private long jobId;
  /**
   * 执行器ID
   */
  private long executorId;
  /**
   * 触发类型
   */
  @Nonnull
  private TriggerTypeEnum triggerType = TriggerTypeEnum.MANUAL;
  /**
   * 执行器任务handler
   */
  @Nonnull
  private String executorHandler = "";
  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam = "";
  /**
   * 执行器路由策略
   */
  @Nonnull
  private LbStrategyEnum routeStrategy = LbStrategyEnum.ROUND_ROBIN;
  /**
   * 阻塞处理策略
   */
  @Nonnull
  private BlockStrategyEnum blockStrategy = BlockStrategyEnum.SERIAL;
  /**
   * 失败重试次数
   */
  private int retryCount = -1;

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
  public TriggerTypeEnum getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(@Nonnull TriggerTypeEnum triggerType) {
    this.triggerType = triggerType;
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
  public LbStrategyEnum getRouteStrategy() {
    return routeStrategy;
  }

  public void setRouteStrategy(@Nonnull LbStrategyEnum routeStrategy) {
    this.routeStrategy = routeStrategy;
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
}
