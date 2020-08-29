package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService

/**
 * @author 宋志宗
 * @date 2020/8/30
 */
@Component("httpExecuteHandler")
final class HttpExecuteHandler(instanceService: JobInstanceService,
                               jobCallbackThreadPool: ExecutorService)
  : BaseHttpExecuteHandler(instanceService, jobCallbackThreadPool) {

  init {
    ExecuteHandlerFactory.register(ExecuteTypeEnum.HTTP_SCRIPT, this)
  }
}