package cn.sh.ideal.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.job.common.executor.JobExecutor
import cn.sh.ideal.job.common.loadbalancer.LbFactory
import cn.sh.ideal.job.common.message.MessageType
import cn.sh.ideal.job.common.message.SocketMessage
import cn.sh.ideal.job.common.message.payload.RegisterCallback
import cn.sh.ideal.job.common.message.payload.RegisterParam
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerProperties
import cn.sh.ideal.job.scheduler.core.socket.SocketJobExecutor
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandlerFactory
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
    private val lbFactory: LbFactory<JobExecutor>,
    private val jobSchedulerProperties: JobSchedulerProperties) : MessageHandler {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    MessageHandlerFactory.register(MessageType.REGISTER, this)
  }

  override fun execute(executor: SocketJobExecutor, socketMessage: SocketMessage) {
    val payload = socketMessage.payload
    val appName = executor.appName
    val instanceId = executor.instanceId
    if (executor.isRegistered) {
      log.warn("appName: {}, instanceId: {}, 客户端重复注册, 已忽略该消息", appName, instanceId)
      return
    }
    val serverHolder = lbFactory.getServerHolder(appName)
    val accessToken = jobSchedulerProperties.accessToken
    val registerParam = try {
      RegisterParam.parseMessage(payload)
    } catch (e: Exception) {
      log.error("解析服务注册参数出现异常: {}", e.message)
      return
    }
    val callback = RegisterCallback()
    callback.messageId = socketMessage.messageId
    if (accessToken.isNotBlank()) {
      val registerToken = registerParam.accessToken
      if (accessToken != registerToken) {
        log.warn("appName: {}, instanceId: {} 请求token不合法: {}",
            appName, instanceId, registerToken)
        callback.isSuccess = false
        callback.message = "accessToken不合法"
        val callbackMessage = SocketMessage(RegisterCallback.typeCode,
            callback.toMessageString())
        executor.sendMessage(callbackMessage.toMessageString())
        executor.destroy()
        return
      }
    }
    executor.weight = registerParam.weight
    executor.isRegistered = true
    serverHolder.addServers(listOf(executor), true)
    log.info("客户端完成注册, appName: {}, instanceId: {}, 注册参数: {}",
        appName, instanceId, payload)
    callback.isSuccess = true
    callback.message = "success"
    val callbackMessage = SocketMessage(RegisterCallback.typeCode,
        callback.toMessageString())
    executor.sendMessage(callbackMessage.toMessageString())
  }
}