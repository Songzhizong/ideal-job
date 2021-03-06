package com.zzsong.job.scheduler.core.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zzsong.job.common.utils.DateTimes;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/9/9
 */
@Getter
@Setter
public class JobInstanceVo {
  /**
   * 任务实例ID
   */
  private long instanceId;

  /**
   * 父实例ID
   */
  private long parentId;

  /**
   * 任务Id
   */
  private long jobId;
  /**
   * 任务名称
   */
  @Nonnull
  private String jobName;

  /**
   * 执行器Id
   */
  private long workerId;

  /**
   * 触发类型
   */
  @Nonnull
  private String triggerType;

  /**
   * 调度机器
   */
  @Nonnull
  private String schedulerInstance;

  /**
   * 执行器任务handler
   */
  @Nonnull
  private String executorHandler;

  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam;

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
  private String executorInstance;

  /**
   * 调度-结果
   */
  private int dispatchStatus;

  /**
   * 调度信息
   */
  @Nonnull
  private String dispatchMsg;

  // -------------------------- 执行信息
  /**
   * 执行时间
   */
  @Nonnull
  private String handleTime;

  /**
   * 完成时间
   */
  @Nonnull
  private String finishedTime;

  @Nonnull
  private String timeConsuming;

  /**
   * 执行状态
   */
  private int handleStatus;

  /**
   * 执行信息
   */
  @Nonnull
  private String result;
}
