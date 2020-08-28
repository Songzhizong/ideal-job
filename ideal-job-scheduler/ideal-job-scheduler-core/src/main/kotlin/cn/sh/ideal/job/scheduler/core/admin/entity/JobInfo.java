package cn.sh.ideal.job.scheduler.core.admin.entity;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.ExecuteTypeEnum;
import cn.sh.ideal.job.common.constants.RouteStrategyEnum;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务信息
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@SuppressWarnings("unused")
@Entity
@Table(
    name = "job_info",
    indexes = {
        @Index(name = "application", columnList = "application"),
        @Index(name = "tenant_id", columnList = "tenant_id"),
        @Index(name = "executor_id", columnList = "executor_id"),
        @Index(name = "biz_type", columnList = "biz_type"),
        @Index(name = "custom_tag", columnList = "custom_tag"),
        @Index(name = "business_id", columnList = "business_id"),
        @Index(name = "job_name", columnList = "job_name"),
        @Index(name = "executor_handler", columnList = "executor_handler"),
        @Index(name = "next_trigger_time", columnList = "next_trigger_time"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "job_info", comment = "任务信息")
@SQLDelete(sql = "update job_info set deleted = 1 where job_id = ?")
@Where(clause = "deleted = 0")
@EntityListeners(AuditingEntityListener.class)
public class JobInfo {
  public static final int JOB_START = 1;
  public static final int JOB_STOP = 0;
  /**
   * 任务Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_info_generator")
  @GenericGenerator(name = "job_info_generator",
      strategy = "cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_info")})
  @Column(name = "job_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '任务Id'"
  )
  @Nonnull
  private Long jobId;

  /**
   * 所属应用
   */
  @Column(
      name = "application", nullable = false, updatable = false, length = 64
      , columnDefinition = "varchar(64) comment '所属应用'"
  )
  @Nonnull
  private String application;

  /**
   * 租户ID
   */
  @Column(
      name = "tenant_id", nullable = false, updatable = false, length = 64
      , columnDefinition = "varchar(64) comment '租户ID'"
  )
  @Nonnull
  private String tenantId;

  /**
   * 业务分类
   */
  @Column(
      name = "biz_type", nullable = false, updatable = false, length = 64
      , columnDefinition = "varchar(64) comment '业务分类'"
  )
  @Nonnull
  private String bizType;

  /**
   * 业务方自定义标签
   */
  @Column(
      name = "custom_tag", nullable = false, updatable = false, length = 64
      , columnDefinition = "varchar(64) comment '业务方自定义标签'"
  )
  @Nonnull
  private String customTag;

  /**
   * 业务方Id
   */
  @Column(
      name = "business_id", nullable = false, updatable = false, length = 64
      , columnDefinition = "varchar(64) comment '业务方Id'"
  )
  @Nonnull
  private String businessId;

  /**
   * 所属执行器Id
   */
  @Column(name = "executor_id", nullable = false
      , columnDefinition = "bigint(20) comment '所属执行器Id'"
  )
  private long executorId;

  /**
   * 任务执行CRON
   */
  @Column(
      name = "cron", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '任务执行CRON'"
  )
  @Nonnull
  private String cron;

  /**
   * 任务名称
   */
  @Column(
      name = "job_name", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '任务名称'"
  )
  @Nonnull
  private String jobName;

  /**
   * 告警邮件地址
   */
  @Column(
      name = "alarm_email", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '告警邮件地址'"
  )
  @Nonnull
  private String alarmEmail;

  /**
   * 执行器路由策略
   */
  @Enumerated(EnumType.STRING)
  @Column(
      name = "route_strategy", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行器路由策略'"
  )
  @Nonnull
  private RouteStrategyEnum routeStrategy;

  /**
   * 执行模式
   */
  @Enumerated(EnumType.STRING)
  @Column(
      name = "execute_type", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行模式'"
  )
  @Nonnull
  private ExecuteTypeEnum executeType;

  /**
   * JobHandler
   */
  @Column(
      name = "executor_handler", nullable = false, length = 128
      , columnDefinition = "varchar(128) comment 'JobHandler'"
  )
  @Nonnull
  private String executorHandler;

  /**
   * 执行参数
   */
  @Column(
      name = "executor_param", nullable = false, length = 8000
      , columnDefinition = "varchar(8000) comment '执行器任务handler'"
  )
  @Nonnull
  private String executorParam;

  /**
   * 阻塞处理策略
   */
  @Enumerated(EnumType.STRING)
  @Column(
      name = "block_strategy", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '阻塞处理策略'"
  )
  @Nonnull
  private BlockStrategyEnum blockStrategy;

  /**
   * 失败重试次数
   */
  @Column(
      name = "retry_count", nullable = false
      , columnDefinition = "int(11) comment '失败重试次数'"
  )
  private int retryCount;

  /**
   * 子任务ID，多个逗号分隔
   */
  @Column(
      name = "child_job_id", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '子任务ID，多个逗号分隔'"
  )
  @Nonnull
  private String childJobId;

  /**
   * 任务状态：0-停止，1-运行
   */
  @Column(
      name = "job_status", nullable = false
      , columnDefinition = "int(11) comment '任务状态：0-停止，1-运行'"
  )
  private int jobStatus;

  /**
   * 上次调度时间
   */
  @Column(name = "last_trigger_time", nullable = false
      , columnDefinition = "bigint(20) comment '上次调度时间'"
  )
  private long lastTriggerTime;

  /**
   * 下次调度时间
   */
  @Column(name = "next_trigger_time", nullable = false
      , columnDefinition = "bigint(20) comment '下次调度时间'"
  )
  private long nextTriggerTime;

  /**
   * 创建时间
   */
  @CreatedDate
  @Column(name = "created_time", nullable = false, updatable = false)
  private LocalDateTime createdTime;

  /**
   * 更新时间
   */
  @LastModifiedDate
  @Column(name = "update_time", nullable = false)
  private LocalDateTime updateTime;

  /**
   * 删除状态:0未删除,1删除
   */
  @Column(
      name = "deleted", nullable = false
      , columnDefinition = "int(11) comment '删除状态:0未删除,1删除'"
  )
  private int deleted;

  @Nonnull
  public Long getJobId() {
    return jobId;
  }

  public void setJobId(@Nonnull Long jobId) {
    this.jobId = jobId;
  }

  @Nonnull
  public String getApplication() {
    return application;
  }

  public void setApplication(@Nonnull String platform) {
    this.application = platform;
  }

  @Nonnull
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(@Nonnull String tenantId) {
    this.tenantId = tenantId;
  }

  @Nonnull
  public String getBizType() {
    return bizType;
  }

  public void setBizType(@Nonnull String bizType) {
    this.bizType = bizType;
  }

  @Nonnull
  public String getCustomTag() {
    return customTag;
  }

  public void setCustomTag(@Nonnull String customTag) {
    this.customTag = customTag;
  }

  @Nonnull
  public String getBusinessId() {
    return businessId;
  }

  public void setBusinessId(@Nonnull String businessId) {
    this.businessId = businessId;
  }

  public long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(long executorId) {
    this.executorId = executorId;
  }

  @Nonnull
  public ExecuteTypeEnum getExecuteType() {
    return executeType;
  }

  public void setExecuteType(@Nonnull ExecuteTypeEnum executeType) {
    this.executeType = executeType;
  }

  @Nonnull
  public String getCron() {
    return cron;
  }

  public void setCron(@Nonnull String cron) {
    this.cron = cron;
  }

  @Nonnull
  public String getJobName() {
    return jobName;
  }

  public void setJobName(@Nonnull String description) {
    this.jobName = description;
  }

  @Nonnull
  public String getAlarmEmail() {
    return alarmEmail;
  }

  public void setAlarmEmail(@Nonnull String alarmEmail) {
    this.alarmEmail = alarmEmail;
  }

  @Nonnull
  public RouteStrategyEnum getRouteStrategy() {
    return routeStrategy;
  }

  public void setRouteStrategy(@Nonnull RouteStrategyEnum routeStrategy) {
    this.routeStrategy = routeStrategy;
  }

  @Nonnull
  public String getExecutorHandler() {
    return executorHandler;
  }

  public void setExecutorHandler(@Nonnull String executorHandler) {
    this.executorHandler = executorHandler;
  }

  @Nonnull
  public String getExecutorParam() {
    return executorParam;
  }

  public void setExecutorParam(@Nonnull String executorParam) {
    this.executorParam = executorParam;
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

  @Nonnull
  public String getChildJobId() {
    return childJobId;
  }

  public void setChildJobId(@Nonnull String childJobId) {
    this.childJobId = childJobId;
  }

  public int getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(int jobStatus) {
    this.jobStatus = jobStatus;
  }

  public long getLastTriggerTime() {
    return lastTriggerTime;
  }

  public void setLastTriggerTime(long lastTriggerTime) {
    this.lastTriggerTime = lastTriggerTime;
  }

  public long getNextTriggerTime() {
    return nextTriggerTime;
  }

  public void setNextTriggerTime(long nextTriggerTime) {
    this.nextTriggerTime = nextTriggerTime;
  }

  public LocalDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(LocalDateTime createdTime) {
    this.createdTime = createdTime;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

  public int getDeleted() {
    return deleted;
  }

  public void setDeleted(int deleted) {
    this.deleted = deleted;
  }
}
