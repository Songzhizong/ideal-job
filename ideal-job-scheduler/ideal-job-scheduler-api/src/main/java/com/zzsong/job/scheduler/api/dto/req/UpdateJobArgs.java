package com.zzsong.job.scheduler.api.dto.req;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.DBDefaults;
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
public class UpdateJobArgs {

    /**
     * 任务ID
     */
    @Nonnull
    @NotNull(message = "jobId不能为空")
    private Long jobId;
    /**
     * 所属执行器Id
     */
    @Nonnull
    @NotNull(message = "所属执行器id不能为空")
    private Long executorId;
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
     * 阻塞策略
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
}
