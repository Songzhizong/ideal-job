package cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbFactory
import cn.sh.ideal.nj.cmpt.job.common.pojo.SocketMessage
import cn.sh.ideal.nj.cmpt.job.common.pojo.payload.RegisterParam
import cn.sh.ideal.nj.cmpt.job.common.utils.JsonUtils
import cn.sh.ideal.nj.cmpt.job.scheduler.core.conf.JobSchedulerProperties
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.SocketExecutor
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.MessageHandlerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 注册信息处理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component("registerMessageHandler")
final class RegisterMessageHandler(
    private val lbFactory: LbFactory,
    private val jobSchedulerProperties: JobSchedulerProperties) : MessageHandler {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    MessageHandlerFactory.register(SocketMessage.Type.REGISTER, this)
  }

  override fun execute(executor: SocketExecutor, messagePayload: String) {
    val appName = executor.appName
    val instanceId = executor.instanceId
    val serverHolder = lbFactory.getServerHolder(appName)
    val accessToken = jobSchedulerProperties.accessToken
    val registerParam = try {
      JsonUtils.parseJson(messagePayload, RegisterParam::class.java)
    } catch (e: Exception) {
      log.error("解析服务注册参数出现异常: {}", e.message)
      return
    }
    if (accessToken.isNotBlank()) {
      val registerToken = registerParam.accessToken
      if (accessToken != registerToken) {
        log.warn("appName: {}, instanceId: {} 请求token不合法: {}",
            appName, instanceId, registerToken)
        // todo token不合法需要将服务移除掉
      }
    }
    executor.weight = registerParam.weight
    executor.isRegistered = true
    serverHolder.markServerReachable(executor)
  }
}