package com.zzsong.job.scheduler.core.admin.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
 * 执行器
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@SuppressWarnings("unused")
@Entity
@Table(
    name = "ideal_job_executor",
    indexes = {
        @Index(name = "uk_app_name", columnList = "appName", unique = true),
        @Index(name = "title", columnList = "title"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Table(appliesTo = "ideal_job_executor", comment = "执行器")
@SQLDelete(sql = "update ideal_job_executor set deleted = 1 where executor_id = ?")
@Where(clause = "deleted = 0")
public class JobExecutorDo {
  /**
   * 执行器Id
   */
  @Id
  @Nonnull
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_executor_generator")
  @GenericGenerator(name = "job_executor_generator",
      strategy = "com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator")
  @Column(nullable = false, updatable = false)
  private Long executorId;

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


