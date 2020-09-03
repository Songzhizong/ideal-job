package cn.sh.ideal.job.scheduler.core.socket.weosocket;

import cn.sh.ideal.job.common.exception.ParseException;
import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.message.SocketMessage;
import cn.sh.ideal.job.common.worker.TaskWorker;
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerProperties;
import cn.sh.ideal.job.scheduler.core.socket.weosocket.handler.MessageHandler;
import cn.sh.ideal.job.scheduler.core.socket.weosocket.handler.MessageHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component
@ServerEndpoint("/websocket/executor/{appName}/{instanceId}")
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private static LbFactory<TaskWorker> lbFactory;
    private static JobSchedulerProperties properties;

    @Autowired
    public void setLbFactory(LbFactory<TaskWorker> lbFactory) {
        WebSocketServer.lbFactory = lbFactory;
    }

    @Autowired
    public void setProperties(JobSchedulerProperties properties) {
        WebSocketServer.properties = properties;
    }

    private Session session;
    private WebsocketTaskWorker websocketTaskWorker;

    @OnOpen
    public void onOpen(@Nonnull Session session,
                       @PathParam("appName") String appName,
                       @PathParam("instanceId") String instanceId) {
        WebsocketTaskWorker worker = new WebsocketTaskWorker(appName, instanceId, session);
        int weightRegisterSeconds = properties.getWeightRegisterSeconds();
        worker.setWeightRegisterSeconds(weightRegisterSeconds);
        this.session = session;
        this.websocketTaskWorker = worker;
        log.info("app: {}, instanceId: {}, sessionId: {} 已建立连接",
                appName, instanceId, session.getId());
    }

    @OnClose
    public void onClose() {
        String appName = websocketTaskWorker.getAppName();
        String instanceId = websocketTaskWorker.getInstanceId();
        LbServerHolder<TaskWorker> serverHolder = lbFactory.getServerHolder(appName);
        serverHolder.removeServer(websocketTaskWorker);
        log.info("app: {}, instanceId: {}, sessionId: {} 下线",
                appName, instanceId, session.getId());
    }

    @OnMessage
    public void onMessage(@Nonnull String message) {
        SocketMessage socketMessage;
        try {
            socketMessage = SocketMessage.parseMessage(message);
        } catch (ParseException e) {
            log.warn("客户端消息解析出现异常: {}", e.getMessage());
            return;
        }
        String messageType = socketMessage.getMessageType();
        MessageType type = MessageType.valueOfCode(messageType);
        if (type == null) {
            log.warn("未知的消息类型: {}", messageType);
            return;
        }
        MessageHandler handler = MessageHandlerFactory.getHandler(type);
        if (handler == null) {
            log.error("messageType: {} 缺少处理器", messageType);
            return;
        }
        handler.execute(websocketTaskWorker, socketMessage);
    }

    @OnError
    public void onError(Throwable throwable) {
        websocketTaskWorker.disposeSocketError(throwable);
    }
}
