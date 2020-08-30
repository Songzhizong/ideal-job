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
        @Index(name = "tenant_id", columnList = "tenantId"),
        @Index(name = "executor_id", columnList = "executorId"),
        @Index(name = "biz_type", columnList = "bizType"),
        @Index(name = "custom_tag", columnList = "customTag"),
        @Index(name = "business_id", columnList = "businessId"),
        @Index(name = "job_name", columnList = "jobName"),
        @Index(name = "executor_handler", columnList = "executorHandler"),
        @Index(name = "next_trigger_time", columnList = "nextTriggerTime"),
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
  @Nonnull
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_info_generator")
  @GenericGenerator(name = "job_info_generator",
      strategy = "cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_info")})
  @Column(nullable = false, updatable = false)
  private Long jobId;

  /**
   * 所属执行器Id
   */
  @Column(nullable = false)
  private long executorId;

  /**
   * 任务执行CRON
   */
  @Nonnull
  @Column(nullable = false, length = 200)
  private String cron;

  /**
   * 任务名称
   */
  @Nonnull
  @Column(nullable = false, length = 200)
  private String jobName;

  /**
   * 告警邮件地址
   */
  @Nonnull
  @Column(nullable = false, length = 200)
  private String alarmEmail;

  /**
   * 执行器路由策略
   */
  @Nonnull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RouteStrategyEnum routeStrategy;

  /**
   * 执行模式
   */
  @Nonnull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ExecuteTypeEnum executeType;

  /**
   * JobHandler
   */
  @Nonnull
  @Column(nullable = false, length = 128)
  private String executorHandler;

  /**
   * 执行参数
   */
//  @Lob
  @Nonnull
  @Column(nullable = false, length = 8000)
  private String executeParam;

  /**
   * 阻塞处理策略
   */
  @Nonnull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BlockStrategyEnum blockStrategy;

  /**
   * 失败重试次数
   */
  @Column(nullable = false)
  private int retryCount;

  /**
   * 子任务ID，多个逗号分隔
   */
  @Nonnull
  @Column(nullable = false, length = 32)
  private String childJobId;

  /**
   * 任务状态：0-停止，1-运行
   */
  @Column(nullable = false)
  private int jobStatus;

  /**
   * 上次调度时间
   */
  @Column(nullable = false)
  private long lastTriggerTime;

  /**
   * 下次调度时间
   */
  @Column(nullable = false)
  private long nextTriggerTime;


  // ---------------------------------------------- 扩展查询字段

  /**
   * 所属应用
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 64)
  private String application;

  /**
   * 租户ID
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 64)
  private String tenantId;

  /**
   * 业务分类
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 64)
  private String bizType;

  /**
   * 业务方自定义标签
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 64)
  private String customTag;

  /**
   * 业务方Id
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 64)
  private String businessId;


  // ----------------------------------------------------
  /**
   * 创建时间
   */
  @Nonnull
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdTime;

  /**
   * 更新时间
   */
  @Nonnull
  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updateTime;

  /**
   * 删除状态:0未删除,1删除
   */
  @Column(nullable = false)
  private int deleted;

  @Nonnull
  public Long getJobId() {
    return jobId;
  }

  public void setJobId(@Nonnull Long jobId) {
    this.jobId = jobId;
  }

  public long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(long executorId) {
    this.executorId = executorId;
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

  public void setJobName(@Nonnull String jobName) {
    this.jobName = jobName;
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
  public ExecuteTypeEnum getExecuteType() {
    return executeType;
  }

  public void setExecuteType(@Nonnull ExecuteTypeEnum executeType) {
    this.executeType = executeType;
  }

  @Nonnull
  public String getExecutorHandler() {
    return executorHandler;
  }

  public void setExecutorHandler(@Nonnull String executorHandler) {
    this.executorHandler = executorHandler;
  }

  @Nonnull
  public String getExecuteParam() {
    return executeParam;
  }

  public void setExecuteParam(@Nonnull String executeParam) {
    this.executeParam = executeParam;
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

  @Nonnull
  public String getApplication() {
    return application;
  }

  public void setApplication(@Nonnull String application) {
    this.application = application;
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

  @Nonnull
  public LocalDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(@Nonnull LocalDateTime createdTime) {
    this.createdTime = createdTime;
  }

  @Nonnull
  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(@Nonnull LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

  public int getDeleted() {
    return deleted;
  }

  public void setDeleted(int deleted) {
    this.deleted = deleted;
  }
}
