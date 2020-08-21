package cn.sh.ideal.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.job.common.pojo.SocketMessage
import cn.sh.ideal.job.scheduler.core.socket.SocketExecutor
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandlerFactory
import org.springframework.stereotype.Component

/**
 * 心跳处理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component("heartbeatMessageHandler")
final class HeartbeatMessageHandler : MessageHandler {

  init {
    MessageHandlerFactory.register(SocketMessage.Type.HEARTBEAT, this)
  }

  override fun execute(executor: SocketExecutor, messagePayload: String) {

  }
}