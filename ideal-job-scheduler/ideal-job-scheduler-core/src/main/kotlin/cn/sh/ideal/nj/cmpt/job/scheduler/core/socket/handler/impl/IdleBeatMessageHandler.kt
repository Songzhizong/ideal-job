package cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.nj.cmpt.job.common.pojo.SocketMessage
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.SocketExecutor
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.MessageHandlerFactory
import org.springframework.stereotype.Component

/**
 * 空闲测试数据处理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component("idleBeatMessageHandler")
final class IdleBeatMessageHandler : MessageHandler {

  init {
    MessageHandlerFactory.register(SocketMessage.Type.IDLE_BEAT, this)
  }

  override fun execute(executor: SocketExecutor, messagePayload: String) {

  }
}