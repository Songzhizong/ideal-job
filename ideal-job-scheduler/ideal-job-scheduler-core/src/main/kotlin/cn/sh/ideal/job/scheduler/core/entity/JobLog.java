package cn.sh.ideal.job.scheduler.core.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 调度日志
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Entity
@Table(
    name = "job_log",
    indexes = {
        @Index(name = "trigger_time", columnList = "trigger_time"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "job_log", comment = "调度日志")
public class JobLog {
  /**
   * 日志Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_log_generator")
  @GenericGenerator(name = "job_log_generator",
      strategy = "cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_log")})
  @Column(name = "log_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '日志Id'"
  )
  private Long logId;
  /**
   * 执行器Id
   */
  @Column(name = "executor_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '执行器Id'"
  )
  private Long executorId;

  /**
   * 任务Id
   */
  @Column(name = "job_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '任务Id'"
  )
  private Long jobId;

  /**
   * 执行器实例id，本次执行的地址
   */
  @Column(
      name = "executor_instance_id", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '执行器实例id，本次执行的地址'"
  )
  private String executor_instance_id;

  /**
   * 执行器任务handler
   */
  @Column(
      name = "executor_handler", nullable = false, length = 128
      , columnDefinition = "varchar(128) comment '执行器任务handler'"
  )
  private String executorHandler;

  /**
   * 执行参数
   */
  @Column(
      name = "executor_param", nullable = false, length = 512
      , columnDefinition = "varchar(512) comment '执行器任务handler'"
  )
  private String executorParam;

  /**
   * 执行器任务分片参数，格式如 1/2
   */
  @Column(
      name = "executor_sharding_param", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行器任务分片参数，格式如 1/2'"
  )
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
  private String triggerMsg;

  /**
   * 执行-时间
   */
  @Column(name = "handle_time", nullable = false)
  private LocalDateTime handleTime;

  /**
   * 执行-状态
   */
  @Column(name = "handle_code", nullable = false
      , columnDefinition = "int(11) comment '执行-状态'"
  )
  private int handleCode;

  /**
   * 执行-日志
   */
  @Column(
      name = "handle_msg", nullable = false, length = 60000
      , columnDefinition = "text comment '执行-日志'"
  )
  private String handleMsg;

  /**
   * 告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败
   */
  @Column(name = "alarm_status", nullable = false
      , columnDefinition = "int(11) comment '告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败'"
  )
  private int alarmStatus;

  public Long getLogId() {
    return logId;
  }

  public void setLogId(Long logId) {
    this.logId = logId;
  }

  public Long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(Long executorId) {
    this.executorId = executorId;
  }

  public Long getJobId() {
    return jobId;
  }

  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }

  public String getExecutor_instance_id() {
    return executor_instance_id;
  }

  public void setExecutor_instance_id(String executor_instance_id) {
    this.executor_instance_id = executor_instance_id;
  }

  public String getExecutorHandler() {
    return executorHandler;
  }

  public void setExecutorHandler(String executorHandler) {
    this.executorHandler = executorHandler;
  }

  public String getExecutorParam() {
    return executorParam;
  }

  public void setExecutorParam(String executorParam) {
    this.executorParam = executorParam;
  }

  public String getExecutorShardingParam() {
    return executorShardingParam;
  }

  public void setExecutorShardingParam(String executorShardingParam) {
    this.executorShardingParam = executorShardingParam;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public LocalDateTime getTriggerTime() {
    return triggerTime;
  }

  public void setTriggerTime(LocalDateTime triggerTime) {
    this.triggerTime = triggerTime;
  }

  public int getTriggerCode() {
    return triggerCode;
  }

  public void setTriggerCode(int triggerCode) {
    this.triggerCode = triggerCode;
  }

  public String getTriggerMsg() {
    return triggerMsg;
  }

  public void setTriggerMsg(String triggerMsg) {
    this.triggerMsg = triggerMsg;
  }

  public LocalDateTime getHandleTime() {
    return handleTime;
  }

  public void setHandleTime(LocalDateTime handleTime) {
    this.handleTime = handleTime;
  }

  public int getHandleCode() {
    return handleCode;
  }

  public void setHandleCode(int handleCode) {
    this.handleCode = handleCode;
  }

  public String getHandleMsg() {
    return handleMsg;
  }

  public void setHandleMsg(String handleMsg) {
    this.handleMsg = handleMsg;
  }

  public int getAlarmStatus() {
    return alarmStatus;
  }

  public void setAlarmStatus(int alarmStatus) {
    this.alarmStatus = alarmStatus;
  }
}
