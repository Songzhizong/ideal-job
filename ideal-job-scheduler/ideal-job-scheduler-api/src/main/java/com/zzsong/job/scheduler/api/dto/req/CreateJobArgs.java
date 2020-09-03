package com.zzsong.job.scheduler.api.dto.req;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.DBDefaults;
import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class CreateJobArgs {
    /**
     * 创建后是否自动运行, 默认否
     */
    private boolean autoStart = false;
    /**
     * 所属执行器Id
     */
    @Nonnull
    @NotNull(message = "所属执行器id不能为空")
    private Long executorId;
    /**
     * 执行模式
     */
    @Nonnull
    @NotNull(message = "执行模式不能为空")
    private ExecuteTypeEnum executeType;
    /**
     * JobHandler
     */
    @Nonnull
    private String executorHandler = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 执行参数
     */
    @Nonnull
    private String executeParam = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 路由策略,默认轮询
     */
    @Nonnull
    private RouteStrategyEnum routeStrategy = RouteStrategyEnum.ROUND_ROBIN;
    /**
     * 阻塞策略, 默认串行执行
     */
    @Nonnull
    private BlockStrategyEnum blockStrategy = BlockStrategyEnum.PARALLEL;
    /**
     * 任务执行CRON
     */
    @Nonnull
    private String cron = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 失败重试次数, 默认不重试
     */
    private int retryCount = DBDefaults.DEFAULT_INT_VALUE;
    /**
     * 任务名称
     */
    @Nonnull
    private String jobName = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 告警邮件地址
     */
    @Nonnull
    private String alarmEmail = DBDefaults.DEFAULT_STRING_VALUE;

    // ---------------------------- 以下为扩展查询字段, 适用于各种业务场景的查询需求

    /**
     * 所属应用, 用于多应用隔离
     */
    @Nonnull
    private String application = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 所属租户, 用于saas系统租户隔离
     */
    @Nonnull
    private String tenantId = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 所属业务
     */
    @Nonnull
    private String bizType = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 自定义标签
     */
    @Nonnull
    private String customTag = DBDefaults.DEFAULT_STRING_VALUE;
    /**
     * 业务id
     */
    @Nonnull
    private String businessId = DBDefaults.DEFAULT_STRING_VALUE;
}
