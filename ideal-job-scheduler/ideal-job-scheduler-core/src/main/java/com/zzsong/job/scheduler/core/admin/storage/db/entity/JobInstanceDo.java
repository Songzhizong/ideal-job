package com.zzsong.job.scheduler.core.admin.storage.db.entity;

import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
 * @author 宋志宗 on 2020/8/20
 */
@SuppressWarnings("unused")
@Entity
@Table(
    name = "ideal_job_instance",
    indexes = {
        @Index(name = "parent_id", columnList = "parentId"),
        @Index(name = "job_id", columnList = "jobId"),
        @Index(name = "worker_Id", columnList = "workerId"),
        @Index(name = "handle_status", columnList = "handleStatus"),
        @Index(name = "created_time", columnList = "createdTime"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Table(appliesTo = "ideal_job_instance", comment = "任务实例")
@EntityListeners(AuditingEntityListener.class)
public class JobInstanceDo {

  /**
   * 任务实例ID
   */
  @Id
  @Nonnull
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_instance_generator")
  @GenericGenerator(name = "job_instance_generator",
      strategy = "com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator")
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
   * 任务名称
   */
  @Nonnull
  @Column(nullable = false, length = 64)
  private String jobName;

  /**
   * 执行器Id
   */
  @Nonnull
  @Column(nullable = false, updatable = false)
  private Long workerId;

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
}
