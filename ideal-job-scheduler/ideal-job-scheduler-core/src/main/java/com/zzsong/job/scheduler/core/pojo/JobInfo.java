package com.zzsong.job.scheduler.core.pojo;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.DBDefaults;
import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/9/5
 */
@Getter
@Setter
public class JobInfo {
  public static final int JOB_START = 1;
  public static final int JOB_STOP = 0;
  /**
   * 任务Id
   */
  @Nonnull
  private Long jobId;
  /**
   * 所属执行器Id
   */
  private long workerId = DBDefaults.DEFAULT_LONG_VALUE;
  /**
   * 任务执行CRON
   */
  @Nonnull
  private String cron = DBDefaults.DEFAULT_STRING_VALUE;

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
  private String executorHandler = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 阻塞处理策略
   */
  @Nonnull
  private BlockStrategyEnum blockStrategy = BlockStrategyEnum.PARALLEL;

  /**
   * 失败重试次数
   */
  private int retryCount = DBDefaults.DEFAULT_INT_VALUE;

  /**
   * 任务状态：0-停止，1-运行
   */
  private int jobStatus = 0;

  /**
   * 上次调度时间
   */
  private long lastTriggerTime = 0;

  /**
   * 下次调度时间
   */
  private long nextTriggerTime = 0;


  // ---------------------------------------------- 扩展查询字段

  /**
   * 所属应用
   */
  @Nonnull
  private String application = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 租户ID
   */
  @Nonnull
  private String tenantId = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * `
   * 业务分类
   */
  @Nonnull
  private String bizType = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 业务方自定义标签
   */
  @Nonnull
  private String customTag = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 业务方Id
   */
  @Nonnull
  private String businessId = DBDefaults.DEFAULT_STRING_VALUE;


  // ----------------------------------------------------
  /**
   * 创建时间
   */
  @Nonnull
  private LocalDateTime createdTime;

  /**
   * 更新时间
   */
  @Nonnull
  private LocalDateTime updateTime;
}
