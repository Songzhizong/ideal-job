package cn.sh.ideal.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.job.common.exception.ParseException
import cn.sh.ideal.job.common.message.MessageType
import cn.sh.ideal.job.common.message.SocketMessage
import cn.sh.ideal.job.common.message.payload.IdleBeatCallback
import cn.sh.ideal.job.scheduler.core.socket.SocketTaskExecutor
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandlerFactory
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 空闲测试数据处理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component("idleBeatCallbackMessageHandler")
final class IdleBeatCallbackMessageHandler : MessageHandler {
  val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    MessageHandlerFactory.register(MessageType.IDLE_BEAT_CALLBACK, this)
  }

  override fun execute(executor: SocketTaskExecutor,
                       socketMessage: SocketMessage) {
    val payload = socketMessage.payload
    val idleBeatCallback = try {
      IdleBeatCallback.parseMessage(payload)
    } catch (e: ParseException) {
      val cause = e.cause
      val errMsg = cause!!.javaClass.name + ":" + e.message
      log.warn("解析IdleBeatCallback出现异常: {}, payload = {}", errMsg, payload)
      return
    }
    val jobId = idleBeatCallback.jobId
    val idleLevel = idleBeatCallback.idleLevel
    if (StringUtils.isBlank(jobId)) {
      executor.setNoneJobIdleLevel(idleLevel)
    } else {
      executor.putJobIdleLevel(jobId, idleLevel)
    }
  }
}