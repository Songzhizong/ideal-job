package com.zzsong.job.scheduler.core.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/9/5
 */
@Getter
@Setter
public class JobWorker {
  /**
   * 执行器Id
   */
  @Nonnull
  private Long workerId;

  /**
   * 执行器AppName
   */
  @Nonnull
  private String appName;

  /**
   * 执行器名称
   */
  @Nonnull
  private String title;

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
