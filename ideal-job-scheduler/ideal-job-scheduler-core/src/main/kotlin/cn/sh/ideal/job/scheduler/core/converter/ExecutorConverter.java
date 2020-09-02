package cn.sh.ideal.job.scheduler.core.converter;

import cn.sh.ideal.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
public final class ExecutorConverter {
    @Nonnull
    public static ExecutorInfoRsp toExecutorInfoRsp(@Nonnull JobExecutor executor) {
        ExecutorInfoRsp executorInfoRsp = new ExecutorInfoRsp();
        executorInfoRsp.setExecutorId(executor.getExecutorId());
        executorInfoRsp.setAppName(executor.getAppName());
        executorInfoRsp.setTitle(executor.getTitle());
        executorInfoRsp.setCreatedTime(executor.getCreatedTime());
        executorInfoRsp.setUpdateTime(executor.getUpdateTime());
        return executorInfoRsp;
    }
}
