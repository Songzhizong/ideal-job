package cn.sh.ideal.job.scheduler.core.socket.weosocket

import cn.sh.ideal.job.common.worker.TaskWorker
import cn.sh.ideal.job.common.loadbalancer.LbFactory
import cn.sh.ideal.job.common.message.MessageType
import cn.sh.ideal.job.common.message.SocketMessage
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerProperties
import cn.sh.ideal.job.scheduler.core.socket.weosocket.handler.MessageHandlerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.websocket.*
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component
@ServerEndpoint("/websocket/executor/{appName}/{instanceId}")
class WebSocketServer {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(WebSocketServer::class.java)
    private lateinit var lbFactory: LbFactory<TaskWorker>
    private lateinit var properties: JobSchedulerProperties
  }

  private lateinit var session: Session
  private lateinit var websocketExecutor: WebsocketTaskWorker

  @Autowired
  fun setLbFactory(lbFactory: LbFactory<TaskWorker>) {
    Companion.lbFactory = lbFactory
  }

  @Autowired
  fun setJobSchedulerProperties(properties: JobSchedulerProperties) {
    Companion.properties = properties
  }

  /**
   * 建立连接处理
   */
  @OnOpen
  fun onOpen(session: Session,
             @PathParam("appName") appName: String,
             @PathParam("instanceId") instanceId: String) {
    session.maxIdleTimeout
    val executor = WebsocketTaskWorker(appName, instanceId, session)
    val weightRegisterSeconds = properties.weightRegisterSeconds
    executor.setWeightRegisterSeconds(weightRegisterSeconds)
    this.session = session
    this.websocketExecutor = executor
    log.info("app: {}, instanceId: {}, sessionId: {} 已建立连接",
        appName, instanceId, session.id)
  }

  @OnClose
  fun onClose() {
    val appName = websocketExecutor.appName
    val instanceId = websocketExecutor.instanceId
    val serverHolder = lbFactory.getServerHolder(appName)
    serverHolder.removeServer(websocketExecutor)
    log.info("app: {}, instanceId: {}, sessionId: {} 下线",
        appName, instanceId, session.id)
  }

  @OnMessage
  fun onMessage(message: String) {
    val socketMessage = try {
      SocketMessage.parseMessage(message)
    } catch (e: Exception) {
      log.warn("客户端消息解析出现异常: {}", e.message)
      return
    }
    val messageType = socketMessage.messageType
    val type = MessageType.valueOfCode(messageType)
    if (type == null) {
      log.warn("未知的消息类型: {}", messageType)
      return
    }
    val handler = MessageHandlerFactory.getHandler(type)
    if (handler == null) {
      log.error("messageType: {} 缺少处理器", messageType)
      return
    }
    handler.execute(websocketExecutor, socketMessage)
  }

  @OnError
  fun onError(throwable: Throwable) {
    websocketExecutor.disposeSocketError(throwable)
  }
}