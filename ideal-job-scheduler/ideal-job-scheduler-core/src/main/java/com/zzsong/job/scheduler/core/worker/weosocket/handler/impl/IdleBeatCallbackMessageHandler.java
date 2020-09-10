//package com.zzsong.job.scheduler.core.socket.weosocket.handler.impl;
//
//import com.zzsong.job.common.exception.ParseException;
//import com.zzsong.job.common.message.MessageType;
//import com.zzsong.job.common.message.SocketMessage;
//import com.zzsong.job.common.message.payload.IdleBeatCallback;
//import com.zzsong.job.scheduler.core.socket.weosocket.WebsocketTaskWorker;
//import com.zzsong.job.scheduler.core.socket.weosocket.handler.MessageHandler;
//import com.zzsong.job.scheduler.core.socket.weosocket.handler.MessageHandlerFactory;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Nonnull;
//
///**
// * @author 宋志宗
// * @date 2020/9/3
// */
//@Component("idleBeatCallbackMessageHandler")
//public final class IdleBeatCallbackMessageHandler implements MessageHandler {
//    private static final Logger log = LoggerFactory
//            .getLogger(IdleBeatCallbackMessageHandler.class);
//
//    public IdleBeatCallbackMessageHandler() {
//        MessageHandlerFactory.register(MessageType.IDLE_BEAT_CALLBACK, this);
//    }
//
//    @Override
//    public void execute(@Nonnull WebsocketTaskWorker executor,
//                        @Nonnull SocketMessage socketMessage) {
//        String payload = socketMessage.getPayload();
//        IdleBeatCallback beatCallback;
//        try {
//            beatCallback = IdleBeatCallback.parseMessage(payload);
//        } catch (ParseException e) {
//            Throwable cause = e.getCause();
//            String errMsg = cause.getClass().getName() + ": " + cause.getMessage();
//            log.warn("解析 ExecuteJobCallback 出现异常: {}, payload = {}", errMsg, payload);
//            return;
//        }
//        String jobId = beatCallback.getJobId();
//        int idleLevel = beatCallback.getIdleLevel();
//        if (StringUtils.isBlank(jobId)) {
//            executor.setNoneJobIdleLevel(idleLevel);
//        } else {
//            executor.putJobIdleLevel(jobId, idleLevel);
//        }
//    }
//}
