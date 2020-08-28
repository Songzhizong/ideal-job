package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum
import cn.sh.ideal.job.common.constants.RouteStrategyEnum
import cn.sh.ideal.job.common.loadbalancer.HttpAddressLbServer
import cn.sh.ideal.job.common.loadbalancer.SimpleLbFactory
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Component("httpExecuteHandler")
final class HttpExecuteHandler : BaseHttpExecuteHandler() {
  private val lbFactory = SimpleLbFactory<HttpAddressLbServer>()

  init {
    ExecuteHandlerFactory.register(ExecuteTypeEnum.HTTP_SCRIPT, this)
  }

  override fun getAddressList(routeStrategy: RouteStrategyEnum, scriptUrl: String): List<String> {
    val serverList = StringUtils.split(scriptUrl, ",")
        .map { StringUtils.split(it, "/")[1].split(":") }
        .map {
          if (it.size == 1) {
            HttpAddressLbServer(it[0], 0)
          } else {
            HttpAddressLbServer(it[0], it[1].toInt())
          }
        }
    val lbStrategy = routeStrategy.lbStrategy!!
    val loadBalancer = lbFactory.getLoadBalancer("http", lbStrategy)
    val chooseServer = loadBalancer.chooseServer(null, serverList) ?: return emptyList()
    return listOf(chooseServer.hostPort)
  }
}

fun main() {
  val s = "https://192.168.1.181:8921/dasda/dasda/dsafa"
  val split = StringUtils.split(s, "/")
  println(split)
}
