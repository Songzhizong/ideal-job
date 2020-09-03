package com.zzsong.job.scheduler.core.dispatch;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.loadbalancer.LbStrategyEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Getter
@Setter
public class TriggerParam {
    /**
     * 任务Id
     */
    private long jobId;
    /**
     * 执行器ID
     */
    private long executorId;
    /**
     * 触发类型
     */
    @Nonnull
    private TriggerTypeEnum triggerType = TriggerTypeEnum.MANUAL;
    /**
     * 执行器任务handler
     */
    @Nonnull
    private String executorHandler = "";
    /**
     * 执行参数
     */
    @Nonnull
    private String executeParam = "";
    /**
     * 执行器路由策略
     */
    @Nonnull
    private LbStrategyEnum routeStrategy = LbStrategyEnum.ROUND_ROBIN;
    /**
     * 阻塞处理策略
     */
    @Nonnull
    private BlockStrategyEnum blockStrategy = BlockStrategyEnum.SERIAL;
    /**
     * 失败重试次数
     */
    private int retryCount = -1;
}
