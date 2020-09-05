package com.zzsong.job.scheduler.core.dispatch.handler;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.scheduler.api.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.db.entity.JobInstanceDo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
public interface ExecuteHandler {

    /**
     * 调度执行任务
     *
     * @param jobView            任务信息
     * @param triggerType        触发类型
     * @param customExecuteParam 自定义执行参数, 如果为空则使用任务默认配置
     * @author 宋志宗
     * @date 2020/8/28 10:23 下午
     */
    void execute(@Nonnull JobInstanceDo instance,
                 @Nonnull JobView jobView,
                 @Nonnull TriggerTypeEnum triggerType,
                 @Nullable String customExecuteParam);
}
