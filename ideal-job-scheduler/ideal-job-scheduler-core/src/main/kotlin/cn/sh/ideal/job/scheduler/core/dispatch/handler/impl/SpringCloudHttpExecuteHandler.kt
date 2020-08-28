package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum
import cn.sh.ideal.job.common.constants.RouteStrategyEnum
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.ribbon.SpringClientFactory
import org.springframework.stereotype.Component

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Component("springCloudHttpExecuteHandler")
final class SpringCloudHttpExecuteHandler(
    private val springClientFactory: SpringClientFactory?,
    private val jobExecutorService: JobExecutorService) : BaseHttpExecuteHandler() {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    ExecuteHandlerFactory.register(ExecuteTypeEnum.LB_HTTP_SCRIPT, this)
  }

  override fun getAddressList(routeStrategy: RouteStrategyEnum, scriptUrl: String): List<String> {
    if (springClientFactory == null) {
      log.error("springClientFactory 为空, 请检查实发配置注册中心, SpringCloud http script 必须配合注册中心使用")
      throw UnsupportedOperationException("无法获取spring cloud 注册中心信息")
    }

    TODO("Not yet implemented")
  }


}
