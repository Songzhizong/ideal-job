package com.zzsong.job.scheduler.core.socket.weosocket.handler;

import com.zzsong.job.common.message.SocketMessage;
import com.zzsong.job.scheduler.core.socket.weosocket.WebsocketTaskWorker;

import javax.annotation.Nonnull;

/**
 * 消息处理器
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface MessageHandler {

    /**
     * 处理请求消息
     */
    void execute(@Nonnull WebsocketTaskWorker executor,
                 @Nonnull SocketMessage socketMessage);
}
