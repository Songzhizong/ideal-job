package com.zzsong.job.scheduler.core.admin.storage.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 执行器
 *
 * @author 宋志宗 on 2020/8/20
 */
@SuppressWarnings("unused")
@Entity
@Table(
    name = "ideal_job_worker",
    indexes = {
        @Index(name = "app_name", columnList = "appName"),
        @Index(name = "title", columnList = "title"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Table(appliesTo = "ideal_job_worker", comment = "执行器")
@SQLDelete(sql = "update ideal_job_worker set deleted = 1 where worker_id = ?")
@Where(clause = "deleted = 0")
public class JobWorkerDo {
  /**
   * 执行器Id
   */
  @Id
  @Nonnull
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "ideal_job_worker_generator")
  @GenericGenerator(name = "ideal_job_worker_generator",
      strategy = "com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator")
  @Column(nullable = false, updatable = false)
  private Long workerId;

  /**
   * 执行器AppName
   */
  @Nonnull
  @Column(nullable = false, length = 64)
  private String appName;

  /**
   * 执行器名称
   */
  @Nonnull
  @Column(nullable = false, length = 32)
  private String title;

  /**
   * 创建时间
   */
  @Nonnull
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdTime;

  /**
   * 更新时间
   */
  @Nonnull
  @Column(nullable = false)
  private LocalDateTime updateTime;

  /**
   * 删除状态:0未删除,1删除
   */
  @Column(nullable = false)
  private int deleted;
}


