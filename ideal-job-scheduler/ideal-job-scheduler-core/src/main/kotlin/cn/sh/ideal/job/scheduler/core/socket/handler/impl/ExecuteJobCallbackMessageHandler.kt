package cn.sh.ideal.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.job.common.message.MessageType
import cn.sh.ideal.job.common.message.SocketMessage
import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback
import cn.sh.ideal.job.scheduler.core.socket.SocketJobExecutor
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandlerFactory
import cn.sh.ideal.job.scheduler.core.trigger.JobTrigger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService

/**
 * 注册信息处理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component("executeJobCallbackMessageHandler")
final class ExecuteJobCallbackMessageHandler(
    private val jobCallbackThreadPool: ExecutorService,
    private val jobTrigger: JobTrigger) : MessageHandler {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    MessageHandlerFactory.register(MessageType.EXECUTE_JOB_CALLBACK, this)
  }

  override fun execute(executor: SocketJobExecutor, socketMessage: SocketMessage) {
    val payload = socketMessage.payload
    val executeJobCallback = try {
      ExecuteJobCallback.parseMessage(payload)
    } catch (e: Exception) {
      val cause = e.cause
      val errMsg = cause!!.javaClass.name + ":" + e.message
      log.warn("解析 ExecuteJobCallback 出现异常: {}, payload = {}", errMsg, payload)
      return
    }
    jobCallbackThreadPool.execute {
      jobTrigger.triggerCallback(executeJobCallback)
    }
  }
}