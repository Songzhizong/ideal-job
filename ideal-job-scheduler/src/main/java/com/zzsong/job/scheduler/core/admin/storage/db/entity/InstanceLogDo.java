package com.zzsong.job.scheduler.core.admin.storage.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * @author 宋志宗 on 2020/9/15
 */
@Entity
@Table(
    name = "ideal_job_instance_log",
    indexes = {
        @Index(name = "instance_id", columnList = "instanceId"),
        @Index(name = "log_time", columnList = "logTime"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Table(appliesTo = "ideal_job_instance_log", comment = "执行日志")
public class InstanceLogDo {

  /**
   * 日志ID
   */
  @Id
  @Nonnull
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_instance_log_generator")
  @GenericGenerator(name = "job_instance_log_generator",
      strategy = "com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator")
  @Column(nullable = false, updatable = false)
  private Long logId;
  /**
   * 任务实例ID
   */
  @Column(nullable = false, updatable = false)
  private long instanceId;

  /**
   * 日志输出时间
   */
  @Column(nullable = false, updatable = false)
  private long logTime;

  /**
   * Job Handler
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 128)
  private String handler;

  /**
   * 日志内容
   */
  @Nonnull
  @Column(nullable = false, updatable = false, length = 4096)
  private String message;
}
