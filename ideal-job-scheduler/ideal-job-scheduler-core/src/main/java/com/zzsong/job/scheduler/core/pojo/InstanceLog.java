package com.zzsong.job.scheduler.core.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * 任务实例日志
 *
 * @author 宋志宗 on 2020/9/15
 */
@Getter
@Setter
public class InstanceLog {
  /**
   * 日志ID
   */
  @Nonnull
  private Long logId;
  /**
   * 任务实例ID
   */
  private long instanceId;

  /**
   * 日志输出时间
   */
  private long logTime;

  /**
   * Job Handler
   */
  @Nonnull
  private String handler;

  /**
   * 日志内容
   */
  @Nonnull
  private String message;
}
