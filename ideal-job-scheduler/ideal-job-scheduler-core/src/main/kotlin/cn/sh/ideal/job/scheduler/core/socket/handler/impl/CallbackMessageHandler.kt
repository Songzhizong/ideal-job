package cn.sh.ideal.job.scheduler.core.socket.handler.impl

import cn.sh.ideal.job.common.pojo.SocketMessage
import cn.sh.ideal.job.scheduler.core.socket.SocketExecutor
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandler
import cn.sh.ideal.job.scheduler.core.socket.handler.MessageHandlerFactory
import org.springframework.stereotype.Component

/**
 * 任务执行结果回调处理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component("callbackMessageHandler")
final class CallbackMessageHandler : MessageHandler {

  init {
    MessageHandlerFactory.register(SocketMessage.Type.EXECUTE_CALLBACK, this)
  }

  override fun execute(executor: SocketExecutor, messagePayload: String) {

  }
}