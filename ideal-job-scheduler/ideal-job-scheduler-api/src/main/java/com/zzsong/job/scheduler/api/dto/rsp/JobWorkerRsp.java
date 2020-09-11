package com.zzsong.job.scheduler.api.dto.rsp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zzsong.job.common.utils.DateTimes;
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
  @JsonSerialize(using = ToStringSerializer.class)
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
  @JsonFormat(pattern = DateTimes.yyyy_MM_dd_HH_mm_ss, timezone = "GMT+8")
  private LocalDateTime createdTime;
  /**
   * 更新时间
   */
  @Nonnull
  @JsonFormat(pattern = DateTimes.yyyy_MM_dd_HH_mm_ss, timezone = "GMT+8")
  private LocalDateTime updateTime;
}
