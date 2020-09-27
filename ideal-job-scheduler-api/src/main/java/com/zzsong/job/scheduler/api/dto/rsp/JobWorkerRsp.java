package com.zzsong.job.scheduler.api.dto.rsp;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/8/26
 */
@Getter
@Setter
public class JobWorkerRsp {
  /**
   * 执行器Id
   */
  private long workerId;
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
   * 是否在线
   */
  private boolean online = false;
  /**
   * 各个节点的注册情况
   */
  private Map<String, List<String>> nodeRegistry;
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
