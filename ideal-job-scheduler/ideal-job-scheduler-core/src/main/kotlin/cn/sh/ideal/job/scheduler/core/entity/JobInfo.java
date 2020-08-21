package cn.sh.ideal.job.scheduler.core.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务信息
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Entity
@Table(name = "job_info")
@org.hibernate.annotations.Table(appliesTo = "job_info", comment = "任务信息")
@SQLDelete(sql = "update job_info set deleted = 1 where job_id = ?")
@Where(clause = "deleted = 0")
public class JobInfo {
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
  private Long jobId;

  /**
   * 所属执行器Id
   */
  @Column(name = "executor_id", nullable = false
      , columnDefinition = "bigint(20) comment '所属执行器Id'"
  )
  private Long executorId;

  /**
   * 任务执行CRON
   */
  @Column(
      name = "cron", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '任务执行CRON'"
  )
  private String cron;

  /**
   * 任务描述
   */
  @Column(
      name = "description", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '任务描述'"
  )
  private String description;

  /**
   * 作者
   */
  @Column(
      name = "author", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '作者'"
  )
  private String author;

  /**
   * 告警邮件地址
   */
  @Column(
      name = "alarm_email", nullable = false, length = 200
      , columnDefinition = "varchar(200) comment '告警邮件地址'"
  )
  private String alarmEmail;

  /**
   * 执行器路由策略
   */
  @Column(
      name = "route_strategy", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行器路由策略'"
  )
  private String routeStrategy;

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
   * 阻塞处理策略
   */
  @Column(
      name = "block_strategy", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '阻塞处理策略'"
  )
  private String blockStrategy;

  /**
   * 任务执行超时时间，单位秒
   */
  @Column(
      name = "timeout", nullable = false
      , columnDefinition = "int(11) comment '任务执行超时时间，单位秒'"
  )
  private int timeout;

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
  private String childJobId;

  /**
   * 调度状态：0-停止，1-运行
   */
  @Column(
      name = "trigger_status", nullable = false
      , columnDefinition = "int(11) comment '调度状态：0-停止，1-运行'"
  )
  private int triggerStatus;

  /**
   * 上次调度时间
   */
  @Column(name = "trigger_last_time", nullable = false
      , columnDefinition = "bigint(20) comment '上次调度时间'"
  )
  private long triggerLastTime;

  /**
   * 下次调度时间
   */
  @Column(name = "trigger_next_time", nullable = false
      , columnDefinition = "bigint(20) comment '下次调度时间'"
  )
  private long triggerNextTime;


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

  public Long getJobId() {
    return jobId;
  }

  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }

  public Long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(Long executorId) {
    this.executorId = executorId;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getAlarmEmail() {
    return alarmEmail;
  }

  public void setAlarmEmail(String alarmEmail) {
    this.alarmEmail = alarmEmail;
  }

  public String getRouteStrategy() {
    return routeStrategy;
  }

  public void setRouteStrategy(String routeStrategy) {
    this.routeStrategy = routeStrategy;
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

  public String getBlockStrategy() {
    return blockStrategy;
  }

  public void setBlockStrategy(String blockStrategy) {
    this.blockStrategy = blockStrategy;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public String getChildJobId() {
    return childJobId;
  }

  public void setChildJobId(String childJobId) {
    this.childJobId = childJobId;
  }

  public int getTriggerStatus() {
    return triggerStatus;
  }

  public void setTriggerStatus(int triggerStatus) {
    this.triggerStatus = triggerStatus;
  }

  public Long getTriggerLastTime() {
    return triggerLastTime;
  }

  public void setTriggerLastTime(Long triggerLastTime) {
    this.triggerLastTime = triggerLastTime;
  }

  public Long getTriggerNextTime() {
    return triggerNextTime;
  }

  public void setTriggerNextTime(Long triggerNextTime) {
    this.triggerNextTime = triggerNextTime;
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
