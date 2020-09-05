//package com.zzsong.job.scheduler.core.socket.weosocket;
//
//import com.zzsong.job.common.worker.TaskWorker;
//import com.zzsong.job.common.message.MessageType;
//import com.zzsong.job.common.message.SocketMessage;
//import com.zzsong.job.common.message.payload.TaskParam;
//import com.zzsong.job.common.message.payload.IdleBeatParam;
//import lombok.Getter;
//import lombok.Setter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import javax.websocket.Session;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author 宋志宗
// * @date 2020/8/20
// */
//public class WebsocketTaskWorker implements TaskWorker {
//    private static final Logger log = LoggerFactory.getLogger(WebsocketTaskWorker.class);
//    private final long createTime = System.currentTimeMillis();
//    @Getter
//    @Nonnull
//    private final String appName;
//    @Nonnull
//    private final String instanceId;
//    @Nonnull
//    private final Session session;
//    @Setter
//    private int weight = 1;
//    @Setter
//    private volatile int weightRegisterSeconds = 60;
//    @Setter
//    private volatile int noneJobIdleLevel = 0;
//    private final ConcurrentMap<String, Integer> idleLevelMap = new ConcurrentHashMap<>();
//
//    @Getter
//    @Setter
//    private volatile boolean registered = false;
//    private volatile boolean destroyed = false;
//
//    public WebsocketTaskWorker(@Nonnull String appName,
//                               @Nonnull String instanceId,
//                               @Nonnull Session session) {
//        this.appName = appName;
//        this.instanceId = instanceId;
//        this.session = session;
//        new Thread(() -> {
//            while (!registered && !destroyed) {
//                if (System.currentTimeMillis() - createTime > weightRegisterSeconds * 1000) {
//                    this.destroy();
//                    log.info("SocketExecutor 超过 {}秒未注册, 已销毁, appName: {}, instanceId: {}",
//                            weightRegisterSeconds, appName, instanceId);
//                    break;
//                } else {
//                    try {
//                        TimeUnit.SECONDS.sleep(1);
//                    } catch (InterruptedException e) {
//                        log.debug("{}", e.getMessage());
//                    }
//                }
//            }
//        }).start();
//    }
//
//    public void putJobIdleLevel(@Nonnull String jobId, int idleLevel) {
//        idleLevelMap.put(jobId, idleLevel);
//    }
//
//    /**
//     * 处理socket异常
//     *
//     * @param throwable 异常信息
//     * @author 宋志宗
//     * @date 2020/8/20 6:26 下午
//     */
//    public void disposeSocketError(@Nonnull Throwable throwable) {
//        log.info("socket error: {}", throwable.getClass().getName() + ":" + throwable.getMessage());
//    }
//
//    /**
//     * 发送消息
//     */
//    public synchronized void sendMessage(@Nonnull String message) {
//        try {
//            session.getBasicRemote().sendText(message);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 执行任务
//     *
//     * @param param 触发器参数
//     * @author 宋志宗
//     * @date 2020/8/20 6:26 下午
//     */
//    @Override
//    public void execute(@Nonnull TaskParam param) {
//        final String paramString = param.toMessageString();
//        final String executeJobCode = MessageType.EXECUTE_JOB.getCode();
//        final SocketMessage message = new SocketMessage(executeJobCode, paramString);
//        final String messageString = message.toMessageString();
//        sendMessage(messageString);
//    }
//
//    @Nonnull
//    @Override
//    public String getInstanceId() {
//        return instanceId;
//    }
//
//    @Override
//    public boolean heartbeat() {
//        if (destroyed) {
//            return false;
//        }
//        try {
////      String message = HeartbeatMessage.createInstance().toMessageString();
//            session.getBasicRemote().sendPing(ByteBuffer.allocate(0));
//        } catch (Exception e) {
//            String message = e.getClass().getName() + ":" + e.getMessage();
//            log.info("发送心跳消息出现异常: {}", message);
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public int idleBeat(@Nullable Object key) {
//        int level = noneJobIdleLevel;
//        if (key != null) {
//            Integer keyIdleLevel = idleLevelMap.putIfAbsent(key.toString(), 0);
//            assert keyIdleLevel != null;
//            level = keyIdleLevel;
//        }
//        IdleBeatParam param = new IdleBeatParam();
//        if (key != null) {
//            param.setJobId(key.toString());
//        }
//        String paramString = param.toMessageString();
//        SocketMessage message = new SocketMessage(IdleBeatParam.typeCode, paramString);
//        String messageString = message.toMessageString();
//        sendMessage(messageString);
//        return level;
//    }
//
//    @Override
//    public int getWeight() {
//        return weight;
//    }
//
//    @Override
//    public synchronized void destroy() {
//        if (!destroyed) {
//            try {
//                session.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                log.info("Destroy SocketExecutor, appName: {}, instanceId: {}, sessionId: {}",
//                        appName, instanceId, session.getId());
//                destroyed = true;
//            }
//        }
//    }
//}
