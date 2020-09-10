package com.zzsong.job.scheduler.api.dto.req;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.DBDefaults;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import com.zzsong.job.common.exception.VisibleException;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/26
 */
@Getter
@Setter
public class UpdateJobArgs {

  /**
   * 任务ID
   */
  private long jobId = -1;
  /**
   * 所属执行器Id
   */
  private long workerId = -1;
  /**
   * JobHandler
   */
  @Nonnull
  private String executorHandler = DBDefaults.DEFAULT_STRING_VALUE;
  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam = DBDefaults.DEFAULT_STRING_VALUE;
  /**
   * 路由策略,默认轮询
   */
  @Nonnull
  private RouteStrategyEnum routeStrategy = RouteStrategyEnum.ROUND_ROBIN;
  /**
   * 阻塞策略
   */
  @Nonnull
  private BlockStrategyEnum blockStrategy = BlockStrategyEnum.PARALLEL;
  /**
   * 任务执行CRON
   */
  @Nonnull
  private String cron = DBDefaults.DEFAULT_STRING_VALUE;
  /**
   * 失败重试次数, 默认不重试
   */
  private int retryCount = DBDefaults.DEFAULT_INT_VALUE;
  /**
   * 任务名称
   */
  @Nonnull
  private String jobName = DBDefaults.DEFAULT_STRING_VALUE;
  /**
   * 告警邮件地址
   */
  @Nonnull
  private String alarmEmail = DBDefaults.DEFAULT_STRING_VALUE;


  public UpdateJobArgs checkArgs() {
    if (jobId < 1) {
      throw new VisibleException("jobId不合法");
    }
    if (workerId < 1) {
      throw new VisibleException("workerId不合法");
    }
    return this;
  }
}
