package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum
import cn.sh.ideal.job.common.constants.RouteStrategyEnum
import cn.sh.ideal.job.common.loadbalancer.LbServer
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.springframework.cloud.netflix.ribbon.SpringClientFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Component("springCloudHttpExecuteHandler")
final class SpringCloudBaseHttpExecuteHandler(
        instanceService: JobInstanceService,
        jobCallbackThreadPool: ExecutorService,
        private val springClientFactory: SpringClientFactory?,
        private val jobExecutorService: JobExecutorService)
    : BaseHttpExecuteHandler(instanceService, jobCallbackThreadPool) {

    private val virtualHttpServerMap = ConcurrentHashMap<String, VirtualHttpServer>()

    init {
        ExecuteHandlerFactory.register(ExecuteTypeEnum.LB_HTTP_SCRIPT, this)
    }

    override fun getAddressList(jobId: Long, routeStrategy: RouteStrategyEnum, scriptUrl: String): List<String> {
        if (springClientFactory == null) {
            log.error("springClientFactory 为空, 请检查实发配置注册中心, SpringCloud http script 必须配合注册中心使用")
            throw UnsupportedOperationException("无法获取spring cloud 注册中心信息")
        }

        TODO("Not yet implemented")
    }


    class VirtualHttpServer(private val hostPort: String) : LbServer {
        override fun heartbeat(): Boolean {
            return true
        }

        override fun getInstanceId(): String {
            return hostPort
        }
    }
}
