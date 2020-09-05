package com.zzsong.job.scheduler.core.dispatch.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("httpExecuteHandler")
public final class HttpExecuteHandler extends BaseHttpExecuteHandler {

    public HttpExecuteHandler(@Nonnull JobInstanceService instanceService,
                              @Nonnull ExecutorService jobCallbackThreadPool) {
        super(instanceService, jobCallbackThreadPool);
        ExecuteHandlerFactory.register(ExecuteTypeEnum.HTTP_SCRIPT, this);
    }
}
