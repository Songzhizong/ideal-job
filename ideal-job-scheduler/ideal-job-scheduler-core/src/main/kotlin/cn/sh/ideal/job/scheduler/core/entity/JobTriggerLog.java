package cn.sh.ideal.job.scheduler.core.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 触发日志
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Entity
@Table(
    name = "job_trigger_log",
    indexes = {
        @Index(name = "trigger_time", columnList = "trigger_time"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "job_trigger_log", comment = "触发日志")
public class JobTriggerLog {
  /**
   * 触发日志Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_trigger_log_generator")
  @GenericGenerator(name = "job_trigger_log_generator",
      strategy = "cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_trigger_log")})
  @Column(name = "trigger_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '触发日志Id'"
  )
  @Nonnull
  private Long triggerId;
  /**
   * 执行器Id
   */
  @Column(name = "executor_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '执行器Id'"
  )
  @Nonnull
  private Long executorId;

  /**
   * 任务Id
   */
  @Column(name = "job_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '任务Id'"
  )
  @Nonnull
  private Long jobId;

  /**
   * 执行器实例id，本次执行的地址
   */
  @Column(
      name = "executor_instance_id", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '执行器实例id，本次执行的地址'"
  )
  @Nonnull
  private String executor_instance_id;

  /**
   * 执行器任务handler
   */
  @Column(
      name = "executor_handler", nullable = false, length = 128
      , columnDefinition = "varchar(128) comment '执行器任务handler'"
  )
  @Nonnull
  private String executorHandler;

  /**
   * 执行参数
   */
  @Column(
      name = "executor_param", nullable = false, length = 512
      , columnDefinition = "varchar(512) comment '执行器任务handler'"
  )
  @Nonnull
  private String executorParam;

  /**
   * 执行器任务分片参数，格式如 1/2
   */
  @Column(
      name = "executor_sharding_param", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行器任务分片参数，格式如 1/2'"
  )
  @Nonnull
  private String executorShardingParam;

  /**
   * 失败重试次数
   */
  @Column(
      name = "retry_count", nullable = false
      , columnDefinition = "int(11) comment '失败重试次数'"
  )
  private int retryCount;

  /**
   * 调度-时间
   */
  @Column(name = "trigger_time", nullable = false)
  @Nonnull
  private LocalDateTime triggerTime;

  /**
   * 调度-结果
   */
  @Column(name = "trigger_code", nullable = false
      , columnDefinition = "int(11) comment '调度-结果'"
  )
  private int triggerCode;

  /**
   * 调度-日志
   */
  @Column(
      name = "trigger_msg", nullable = false, length = 60000
      , columnDefinition = "text comment '调度-日志'"
  )
  @Nonnull
  private String triggerMsg;

  /**
   * 执行-时间
   */
  @Column(name = "handle_time")
  @Nullable
  private LocalDateTime handleTime;

  /**
   * 执行-状态
   */
  @Column(name = "handle_status_code", nullable = false
      , columnDefinition = "int(11) comment '执行-状态'"
  )
  private int handleStatusCode;

  /**
   * 执行-日志
   */
  @Column(
      name = "handle_msg", nullable = false, length = 60000
      , columnDefinition = "text comment '执行-日志'"
  )
  @Nonnull
  private String handleMsg;

  /**
   * 告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败
   */
  @Column(name = "alarm_status", nullable = false
      , columnDefinition = "int(11) comment '告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败'"
  )
  private int alarmStatus;

  @Nonnull
  public Long getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(@Nonnull Long triggerId) {
    this.triggerId = triggerId;
  }

  @Nonnull
  public Long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(@Nonnull Long executorId) {
    this.executorId = executorId;
  }

  @Nonnull
  public Long getJobId() {
    return jobId;
  }

  public void setJobId(@Nonnull Long jobId) {
    this.jobId = jobId;
  }

  @Nonnull
  public String getExecutor_instance_id() {
    return executor_instance_id;
  }

  public void setExecutor_instance_id(@Nonnull String executor_instance_id) {
    this.executor_instance_id = executor_instance_id;
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
  public String getExecutorShardingParam() {
    return executorShardingParam;
  }

  public void setExecutorShardingParam(@Nonnull String executorShardingParam) {
    this.executorShardingParam = executorShardingParam;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  @Nonnull
  public LocalDateTime getTriggerTime() {
    return triggerTime;
  }

  public void setTriggerTime(@Nonnull LocalDateTime triggerTime) {
    this.triggerTime = triggerTime;
  }

  public int getTriggerCode() {
    return triggerCode;
  }

  public void setTriggerCode(int triggerCode) {
    this.triggerCode = triggerCode;
  }

  @Nonnull
  public String getTriggerMsg() {
    return triggerMsg;
  }

  public void setTriggerMsg(@Nonnull String triggerMsg) {
    this.triggerMsg = triggerMsg;
  }

  @Nullable
  public LocalDateTime getHandleTime() {
    return handleTime;
  }

  public void setHandleTime(@Nullable LocalDateTime handleTime) {
    this.handleTime = handleTime;
  }

  public int getHandleStatusCode() {
    return handleStatusCode;
  }

  public void setHandleStatusCode(int handleCode) {
    this.handleStatusCode = handleCode;
  }

  @Nonnull
  public String getHandleMsg() {
    return handleMsg;
  }

  public void setHandleMsg(@Nonnull String handleMsg) {
    this.handleMsg = handleMsg;
  }

  public int getAlarmStatus() {
    return alarmStatus;
  }

  public void setAlarmStatus(int alarmStatus) {
    this.alarmStatus = alarmStatus;
  }
}
