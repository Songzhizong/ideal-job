package com.zzsong.job.scheduler.core.pojo;

import com.zzsong.job.common.constants.DBDefaults;
import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/9/5
 */
@Getter
@Setter
public class JobInstance {
  public static final int DISPATCH_FAIL = 0;
  public static final int DISPATCH_SUCCESS = 1;

  // -------------------------- 基本信息
  /**
   * 任务实例ID
   */
  @Nonnull
  private Long instanceId;

  /**
   * 父实例ID
   */
  private long parentId = DBDefaults.DEFAULT_LONG_VALUE;

  /**
   * 任务Id
   */
  private long jobId = DBDefaults.DEFAULT_LONG_VALUE;

  /**
   * 任务名称
   */
  @Nonnull
  private String jobName = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 执行器Id
   */
  private long executorId = DBDefaults.DEFAULT_LONG_VALUE;

  /**
   * 触发类型
   */
  @Nonnull
  private TriggerTypeEnum triggerType = TriggerTypeEnum.CRON;

  /**
   * 调度机器
   */
  @Nonnull
  private String schedulerInstance = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 执行器任务handler
   */
  @Nonnull
  private String executorHandler = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam = DBDefaults.DEFAULT_STRING_VALUE;

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

  // -------------------------- 调度信息
  /**
   * 本次执行地址
   */
  @Nonnull
  private String executorInstance = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 调度-结果
   */
  private int dispatchStatus = DISPATCH_FAIL;

  /**
   * 调度信息
   */
  @Nonnull
  private String dispatchMsg = DBDefaults.DEFAULT_STRING_VALUE;

  // -------------------------- 执行信息
  /**
   * 执行时间
   */
  private long handleTime = DBDefaults.DEFAULT_LONG_VALUE;

  /**
   * 完成时间
   */
  private long finishedTime = DBDefaults.DEFAULT_LONG_VALUE;

  /**
   * 执行状态
   */
  @Nonnull
  private HandleStatusEnum handleStatus = HandleStatusEnum.UNKNOWN;

  /**
   * 执行信息
   */
  @Nonnull
  private String result = DBDefaults.DEFAULT_STRING_VALUE;

  /**
   * 执行序列
   */
  private int sequence = DBDefaults.DEFAULT_INT_VALUE;
}
