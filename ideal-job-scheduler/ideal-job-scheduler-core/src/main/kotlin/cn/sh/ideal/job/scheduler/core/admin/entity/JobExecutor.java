package cn.sh.ideal.job.scheduler.core.admin.entity;

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
@Entity
@Table(
    name = "job_executor",
    indexes = {
        @Index(name = "uk_app_name", columnList = "app_name", unique = true),
        @Index(name = "title", columnList = "title"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "job_executor", comment = "执行器")
@SQLDelete(sql = "update job_executor set deleted = 1 where executor_id = ?")
@Where(clause = "deleted = 0")
@EntityListeners(AuditingEntityListener.class)
public class JobExecutor {
  /**
   * 执行器Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_executor_generator")
  @GenericGenerator(name = "job_executor_generator",
      strategy = "cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_executor")})
  @Column(name = "executor_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '执行器Id'"
  )
  @Nonnull
  private Long executorId;

  /**
   * 执行器AppName
   */
  @Column(
      name = "app_name", nullable = false, length = 64
      , columnDefinition = "varchar(64) comment '执行器AppName'"
  )
  @Nonnull
  private String appName;

  /**
   * 执行器名称
   */
  @Column(
      name = "title", nullable = false, length = 32
      , columnDefinition = "varchar(32) comment '执行器名称'"
  )
  @Nonnull
  private String title;

  /**
   * 创建时间
   */
  @CreatedDate
  @Column(name = "created_time", nullable = false, updatable = false)
  @Nonnull
  private LocalDateTime createdTime;

  /**
   * 更新时间
   */
  @LastModifiedDate
  @Column(name = "update_time", nullable = false)
  @Nonnull
  private LocalDateTime updateTime;

  /**
   * 删除状态:0未删除,1删除
   */
  @Column(
      name = "deleted", nullable = false
      , columnDefinition = "int(11) comment '删除状态:0未删除,1删除'"
  )
  private int deleted;

  @Nonnull
  public Long getExecutorId() {
    return executorId;
  }

  public void setExecutorId(@Nonnull Long executorId) {
    this.executorId = executorId;
  }

  @Nonnull
  public String getAppName() {
    return appName;
  }

  public void setAppName(@Nonnull String appName) {
    this.appName = appName;
  }

  @Nonnull
  public String getTitle() {
    return title;
  }

  public void setTitle(@Nonnull String title) {
    this.title = title;
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


