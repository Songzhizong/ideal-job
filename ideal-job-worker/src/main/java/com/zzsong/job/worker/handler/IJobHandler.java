package com.zzsong.job.worker.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
public interface IJobHandler {

    @Nullable
    Object execute(@Nonnull String param) throws Exception;
}
