package com.zzsong.job.scheduler.core.admin.entity;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
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
 * 任务信息
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@SuppressWarnings("unused")
@Entity
@Table(
        name = "ideal_job_info",
        indexes = {
                @Index(name = "application", columnList = "application"),
                @Index(name = "tenant_id", columnList = "tenantId"),
                @Index(name = "executor_id", columnList = "executorId"),
                @Index(name = "biz_type", columnList = "bizType"),
                @Index(name = "custom_tag", columnList = "customTag"),
                @Index(name = "business_id", columnList = "businessId"),
                @Index(name = "job_name", columnList = "jobName"),
                @Index(name = "executor_handler", columnList = "executorHandler"),
                @Index(name = "next_trigger_time", columnList = "nextTriggerTime"),
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Table(appliesTo = "ideal_job_info", comment = "任务信息")
@SQLDelete(sql = "update ideal_job_info set deleted = 1 where job_id = ?")
@Where(clause = "deleted = 0")
@EntityListeners(AuditingEntityListener.class)
public class JobInfo {
    public static final int JOB_START = 1;
    public static final int JOB_STOP = 0;
    /**
     * 任务Id
     */
    @Id
    @Nonnull
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_info_generator")
    @GenericGenerator(name = "job_info_generator",
            strategy = "com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator")
    @Column(nullable = false, updatable = false)
    private Long jobId;

    /**
     * 所属执行器Id
     */
    @Column(nullable = false)
    private long executorId;

    /**
     * 任务执行CRON
     */
    @Nonnull
    @Column(nullable = false, length = 200)
    private String cron;

    /**
     * 任务名称
     */
    @Nonnull
    @Column(nullable = false, length = 200)
    private String jobName;

    /**
     * 告警邮件地址
     */
    @Nonnull
    @Column(nullable = false, length = 200)
    private String alarmEmail;

    /**
     * 执行器路由策略
     */
    @Nonnull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStrategyEnum routeStrategy;

    /**
     * 执行模式
     */
    @Nonnull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExecuteTypeEnum executeType;

    /**
     * JobHandler
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

    /**
     * 阻塞处理策略
     */
    @Nonnull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BlockStrategyEnum blockStrategy;

    /**
     * 失败重试次数
     */
    @Column(nullable = false)
    private int retryCount;

    /**
     * 任务状态：0-停止，1-运行
     */
    @Column(nullable = false)
    private int jobStatus;

    /**
     * 上次调度时间
     */
    @Column(nullable = false)
    private long lastTriggerTime;

    /**
     * 下次调度时间
     */
    @Column(nullable = false)
    private long nextTriggerTime;


    // ---------------------------------------------- 扩展查询字段

    /**
     * 所属应用
     */
    @Nonnull
    @Column(nullable = false, updatable = false, length = 64)
    private String application;

    /**
     * 租户ID
     */
    @Nonnull
    @Column(nullable = false, updatable = false, length = 64)
    private String tenantId;

    /**
     * `
     * 业务分类
     */
    @Nonnull
    @Column(nullable = false, updatable = false, length = 64)
    private String bizType;

    /**
     * 业务方自定义标签
     */
    @Nonnull
    @Column(nullable = false, updatable = false, length = 64)
    private String customTag;

    /**
     * 业务方Id
     */
    @Nonnull
    @Column(nullable = false, updatable = false, length = 64)
    private String businessId;


    // ----------------------------------------------------
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

    /**
     * 删除状态:0未删除,1删除
     */
    @Column(nullable = false)
    private int deleted;
}
