package cn.sh.ideal.job.scheduler.core.admin.entity;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.DBDefaults;
import cn.sh.ideal.job.common.constants.HandleStatusEnum;
import cn.sh.ideal.job.common.constants.TriggerTypeEnum;
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
        @Index(name = "job_id", columnList = "job_id"),
        @Index(name = "created_time", columnList = "created_time"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "job_trigger_log", comment = "触发日志")
@EntityListeners(AuditingEntityListener.class)
public class JobTriggerLog {
  public static final int TRIGGER_CODE_FAIL = 0;
  public static final int TRIGGER_CODE_SUCCESS = 1;

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
   * 触发类型
   */
  @Enumerated(EnumType.STRING)
  @Column(
      name = "trigger_type", nullable = false, length = 16
      , columnDefinition = "varchar(16) comment '触发类型'"
  )
  @Nonnull
  private TriggerTypeEnum triggerType;

  /**
   * 调度机器
   */
  @Column(
      name = "scheduler_instance", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '调度机器'"
  )
  @Nonnull
  private String schedulerInstance;

  /**
   * 可用执行器实例列表, 多个逗号分割
   */
  @Column(
      name = "available_instances", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '执行器实例id, 本次执行的地址, 多个逗号分割'"
  )
  @Nonnull
  private String availableInstances;

  /**
   * 执行器实例列表, 本次执行的地址, 多个逗号分割
   */
  @Column(
      name = "execute_instances", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '执行器实例列表, 本次执行的地址, 多个逗号分割'"
  )
  @Nonnull
  private String executeInstances;

  /**
   * 执行器路由策略
   */
  @Enumerated(EnumType.STRING)
  @Column(
      name = "route_strategy", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行器路由策略'"
  )
  @Nonnull
  private LbStrategyEnum routeStrategy;

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
   * 调度-结果
   */
  @Column(name = "trigger_code", nullable = false
      , columnDefinition = "int(11) comment '调度-结果'"
  )
  private int triggerCode;

  /**
   * 调度信息
   */
  @Column(
      name = "trigger_msg", nullable = false, length = 60000
      , columnDefinition = "text comment '调度信息'"
  )
  @Nonnull
  private String triggerMsg;

  /**
   * 执行时间
   */
  @Column(name = "handle_time")
  private LocalDateTime handleTime;

  /**
   * 执行状态
   */
  @Enumerated(EnumType.STRING)
  @Column(
      name = "handle_status", nullable = false, length = 16
      , columnDefinition = "varchar(16) comment '执行状态'"
  )
  private HandleStatusEnum handleStatus;

  /**
   * 执行信息
   */
  @Column(
      name = "handle_msg", nullable = false, length = 60000
      , columnDefinition = "text comment '执行-日志'"
  )
  @Nonnull
  private String handleMsg;

  /**
   * 失败重试次数
   */
  @Column(
      name = "handle_sequence", nullable = false
      , columnDefinition = "int(11) comment '失败重试次数'"
  )
  private int handleSequence;

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

  @Nonnull
  public static JobTriggerLog createInitialized() {
    JobTriggerLog log = new JobTriggerLog();
    log.schedulerInstance = DBDefaults.DEFAULT_STRING_VALUE;
    log.availableInstances = DBDefaults.DEFAULT_STRING_VALUE;
    log.executeInstances = DBDefaults.DEFAULT_STRING_VALUE;
    log.executorHandler = DBDefaults.DEFAULT_STRING_VALUE;
    log.executorParam = DBDefaults.DEFAULT_STRING_VALUE;
    log.executorShardingParam = DBDefaults.DEFAULT_STRING_VALUE;
    log.triggerMsg = DBDefaults.DEFAULT_STRING_VALUE;
    log.handleStatus = HandleStatusEnum.WAITING;
    log.handleMsg = DBDefaults.DEFAULT_STRING_VALUE;
    log.handleSequence = 0;
    return log;
  }

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
  public String getAvailableInstances() {
    return availableInstances;
  }

  public void setAvailableInstances(@Nonnull String availableInstances) {
    this.availableInstances = availableInstances;
  }

  @Nonnull
  public String getExecuteInstances() {
    return executeInstances;
  }

  public void setExecuteInstances(@Nonnull String executeInstances) {
    this.executeInstances = executeInstances;
  }

  @Nonnull
  public LbStrategyEnum getRouteStrategy() {
    return routeStrategy;
  }

  public void setRouteStrategy(@Nonnull LbStrategyEnum routeStrategy) {
    this.routeStrategy = routeStrategy;
  }

  @Nonnull
  public BlockStrategyEnum getBlockStrategy() {
    return blockStrategy;
  }

  public void setBlockStrategy(@Nonnull BlockStrategyEnum blockStrategy) {
    this.blockStrategy = blockStrategy;
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

  public LocalDateTime getHandleTime() {
    return handleTime;
  }

  public void setHandleTime(LocalDateTime handleTime) {
    this.handleTime = handleTime;
  }

  public HandleStatusEnum getHandleStatus() {
    return handleStatus;
  }

  public void setHandleStatus(HandleStatusEnum handleStatus) {
    this.handleStatus = handleStatus;
  }

  @Nonnull
  public String getHandleMsg() {
    return handleMsg;
  }

  public void setHandleMsg(@Nonnull String handleMsg) {
    this.handleMsg = handleMsg;
  }

  public int getHandleSequence() {
    return handleSequence;
  }

  public void setHandleSequence(int handleSequence) {
    this.handleSequence = handleSequence;
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
}
