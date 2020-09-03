package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl;

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum;
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService;
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("httpExecuteHandler")
public class HttpExecuteHandler extends BaseHttpExecuteHandler {

    public HttpExecuteHandler(@Nonnull JobInstanceService instanceService,
                              @Nonnull ExecutorService jobCallbackThreadPool) {
        super(instanceService, jobCallbackThreadPool);
        ExecuteHandlerFactory.register(ExecuteTypeEnum.HTTP_SCRIPT, this);
    }
}
