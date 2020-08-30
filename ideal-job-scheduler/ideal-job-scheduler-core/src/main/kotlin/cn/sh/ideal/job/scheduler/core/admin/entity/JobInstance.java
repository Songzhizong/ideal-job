package cn.sh.ideal.job.scheduler.core.admin.entity;

import cn.sh.ideal.job.common.constants.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务实例
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@SuppressWarnings("unused")
@Entity
@Table(
    name = "ideal_job_instance",
    indexes = {
        @Index(name = "parent_id", columnList = "parentId"),
        @Index(name = "job_id", columnList = "jobId"),
        @Index(name = "created_time", columnList = "createdTime"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "ideal_job_instance", comment = "任务实例")
@EntityListeners(AuditingEntityListener.class)
public class JobInstance {
  public static final int STATUS_FAIL = 0;
  public static final int STATUS_SUCCESS = 1;

  /**
   * 任务实例ID
   */
  @Id
  @Nonnull
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_trigger_log_generator")
  @GenericGenerator(name = "job_trigger_log_generator",
      strategy = "cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_trigger_log")})
  @Column(nullable = false, updatable = false)
  private Long instanceId;

  /**
   * 父实例ID
   */
  @Column(nullable = false, updatable = false)
  private Long parentId;

  /**
   * 任务Id
   */
  @Nonnull
  @Column(nullable = false, updatable = false)
  private Long jobId;

  /**
   * 执行器Id
   */
  @Nonnull
  @Column(nullable = false, updatable = false)
  private Long executorId;

  /**
   * 触发类型
   */
  @Nonnull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private TriggerTypeEnum triggerType;

  /**
   * 调度机器
   */
  @Nonnull
  @Column(nullable = false, length = 32)
  private String schedulerInstance;

  /**
   * 执行器任务handler
   */
  @Nonnull
  @Column(nullable = false, length = 128)
  private String executorHandler;

  /**
   * 执行参数
   */
  @Lob
  @Nonnull
  @Column(nullable = false)
  private String executeParam;

  // -------------------------- 调度信息
  /**
   * 本次执行地址
   */
  @Nonnull
  @Column(nullable = false, length = 128)
  private String executorInstance;

  /**
   * 调度-结果
   */
  @Column(nullable = false)
  private int dispatchStatus;

  /**
   * 调度信息
   */
  @Nonnull
  @Column(nullable = false, length = 200)
  private String dispatchMsg;

  // -------------------------- 执行信息
  /**
   * 执行时间
   */
  @Column(nullable = false)
  private long handleTime;

  /**
   * 完成时间
   */
  @Column(nullable = false)
  private long finishedTime;

  /**
   * 执行状态
   */
  @Nonnull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private HandleStatusEnum handleStatus;

  /**
   * 执行信息
   */
  @Lob
  @Nonnull
  @Column(nullable = false)
  private String result;

  /**
   * 回调序列
   */
  @Column(nullable = false)
  private int sequence;

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

  @Nonnull
  public static JobInstance createInitialized() {
    JobInstance instance = new JobInstance();
    instance.parentId = DBDefaults.DEFAULT_LONG_VALUE;
    instance.executorInstance = DBDefaults.DEFAULT_STRING_VALUE;
    instance.executorHandler = DBDefaults.DEFAULT_STRING_VALUE;
    instance.executeParam = DBDefaults.DEFAULT_STRING_VALUE;
    instance.dispatchStatus = STATUS_SUCCESS;
    instance.dispatchMsg = DBDefaults.DEFAULT_STRING_VALUE;
    instance.handleStatus = HandleStatusEnum.WAITING;
    instance.result = DBDefaults.DEFAULT_STRING_VALUE;
    instance.sequence = DBDefaults.DEFAULT_INT_VALUE;
    return instance;
  }

  @Nonnull
  public Long getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(@Nonnull Long instanceId) {
    this.instanceId = instanceId;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  @Nonnull
  public Long getJobId() {
    return jobId;
  }

  public void setJobId(@Nonnull Long jobId) {
    this.jobId = jobId;
  }

  @Nonnull
  public Long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(@Nonnull Long executorId) {
    this.executorId = executorId;
  }

  @Nonnull
  public TriggerTypeEnum getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(@Nonnull TriggerTypeEnum triggerType) {
    this.triggerType = triggerType;
  }

  @Nonnull
  public String getSchedulerInstance() {
    return schedulerInstance;
  }

  public void setSchedulerInstance(@Nonnull String schedulerInstance) {
    this.schedulerInstance = schedulerInstance;
  }

  @Nonnull
  public String getExecutorInstance() {
    return executorInstance;
  }

  public void setExecutorInstance(@Nonnull String executeInstance) {
    this.executorInstance = executeInstance;
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

  public int getDispatchStatus() {
    return dispatchStatus;
  }

  public void setDispatchStatus(int triggerCode) {
    this.dispatchStatus = triggerCode;
  }

  @Nonnull
  public String getDispatchMsg() {
    return dispatchMsg;
  }

  public void setDispatchMsg(@Nonnull String triggerMsg) {
    this.dispatchMsg = triggerMsg;
  }

  public long getHandleTime() {
    return handleTime;
  }

  public void setHandleTime(long handleTime) {
    this.handleTime = handleTime;
  }

  public long getFinishedTime() {
    return finishedTime;
  }

  public void setFinishedTime(long finishedTime) {
    this.finishedTime = finishedTime;
  }

  @Nonnull
  public HandleStatusEnum getHandleStatus() {
    return handleStatus;
  }

  public void setHandleStatus(@Nonnull HandleStatusEnum handleStatus) {
    this.handleStatus = handleStatus;
  }

  @Nonnull
  public String getResult() {
    return result;
  }

  public void setResult(@Nonnull String result) {
    this.result = result;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
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
}
