package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.RouteStrategyEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.common.utils.JsonUtils
import cn.sh.ideal.job.scheduler.api.pojo.HttpScript
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandler
import cn.sh.ideal.job.scheduler.core.utils.WebClients
import org.apache.commons.lang3.StringUtils

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
abstract class BaseHttpExecuteHandler : ExecuteHandler {

  private val webClient = WebClients.createWebClient(
      400, 400, 120_000)

  override fun execute(jobInfo: JobInfo,
                       triggerType: TriggerTypeEnum,
                       customExecutorParam: String?): Res<Void> {
    val executorParam = jobInfo.executorParam
    if (StringUtils.isBlank(executorParam)) {
      return Res.err()
    }
    val routeStrategy = jobInfo.routeStrategy
    val httpScript = JsonUtils.parseJson(executorParam, HttpScript::class.java)
    val method = httpScript.method
    val url = httpScript.url
    val headers = httpScript.headers
    val body = httpScript.body

    TODO("Not yet implemented")
  }

  abstract fun getAddressList(routeStrategy: RouteStrategyEnum, scriptUrl: String): List<String>
}