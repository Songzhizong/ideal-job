package com.zzsong.job.scheduler.api.dto.rsp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zzsong.job.common.utils.DateTimes;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/8/26
 */
@Getter
@Setter
public class JobInfoRsp {
  /**
   * 任务Id
   */
  @JsonSerialize(using = ToStringSerializer.class)
  private long jobId;
  /**
   * 所属应用
   */
  @Nonnull
  private String application;
  /**
   * 租户ID
   */
  @Nonnull
  private String tenantId;
  /**
   * 业务分类
   */
  @Nonnull
  private String bizType;
  /**
   * 业务方自定义标签
   */
  @Nonnull
  private String customTag;
  /**
   * 业务方Id
   */
  @Nonnull
  private String businessId;
  /**
   * 所属执行器Id
   */
  @JsonSerialize(using = ToStringSerializer.class)
  private long workerId;
  /**
   * 任务执行CRON
   */
  @Nonnull
  private String cron;
  /**
   * 任务名称
   */
  @Nonnull
  private String jobName;
  /**
   * 告警邮件地址
   */
  @Nonnull
  private String alarmEmail;
  /**
   * 执行器路由策略
   */
  private String routeStrategy;
  /**
   * JobHandler
   */
  @Nonnull
  private String executorHandler;
  /**
   * 执行参数
   */
  @Nonnull
  private String executeParam;
  /**
   * 阻塞处理策略
   */
  @Nonnull
  private String blockStrategy;
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
  @Nullable
  @JsonFormat(pattern = DateTimes.yyyy_MM_dd_HH_mm_ss, timezone = "GMT+8")
  private LocalDateTime lastTriggerTime;
  /**
   * 下次调度时间
   */
  @Nullable
  @JsonFormat(pattern = DateTimes.yyyy_MM_dd_HH_mm_ss, timezone = "GMT+8")
  private LocalDateTime nextTriggerTime;
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
