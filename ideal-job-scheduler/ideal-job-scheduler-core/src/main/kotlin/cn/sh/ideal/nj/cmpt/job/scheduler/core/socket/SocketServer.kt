package cn.sh.ideal.nj.cmpt.job.scheduler.core.socket

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbFactory
import cn.sh.ideal.nj.cmpt.job.common.pojo.SocketMessage
import cn.sh.ideal.nj.cmpt.job.common.utils.JsonUtils
import cn.sh.ideal.nj.cmpt.job.scheduler.core.conf.JobSchedulerProperties
import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler.MessageHandlerFactory
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
class SocketServer {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(SocketServer::class.java)
    private lateinit var lbFactory: LbFactory
    private lateinit var properties: JobSchedulerProperties
  }

  private lateinit var session: Session
  private lateinit var socketExecutor: SocketExecutor

  @Autowired
  fun setLbFactory(lbFactory: LbFactory) {
    SocketServer.lbFactory = lbFactory
  }

  @Autowired
  fun setJobSchedulerProperties(properties: JobSchedulerProperties) {
    SocketServer.properties = properties
  }

  /**
   * 建立连接处理
   */
  @OnOpen
  fun onOpen(session: Session,
             @PathParam("appName") appName: String,
             @PathParam("instanceId") instanceId: String) {
    val executor = SocketExecutor(appName, instanceId, session)
    val weightRegisterSeconds = properties.weightRegisterSeconds
    executor.weightRegisterSeconds = weightRegisterSeconds
    this.session = session
    this.socketExecutor = executor
    log.info("app: {}, instanceId: {}, sessionId: {} 已建立连接", appName, instanceId, session.id)
  }

  @OnClose
  fun onClose() {
    val appName = socketExecutor.appName
    val instanceId = socketExecutor.instanceId
    val serverHolder = lbFactory.getServerHolder(appName)
    serverHolder.removeServer(socketExecutor)
    log.info("app: {}, instanceId: {}, sessionId: {} 下线", appName, instanceId, session.id)
  }

  @OnMessage
  fun onMessage(message: String) {
    log.debug("接收到客户端消息: {}", message)
    val socketMessage = JsonUtils.parseJson(message, SocketMessage::class.java)
    val messageType = socketMessage.messageType
    val payload = socketMessage.payload
    val type = SocketMessage.Type.valueOfCode(messageType)
    if (type == null) {
      log.warn("未知的消息类型: {}", messageType)
      return
    }
    val handler = MessageHandlerFactory.getHandler(type)
    if (handler == null) {
      log.error("messageType: {} 缺少处理器", type.name)
      return
    }
    handler.execute(socketExecutor, payload)
  }

  @OnError
  fun onError(throwable: Throwable) {
    socketExecutor.disposeSocketError(throwable)
  }
}