package cn.sh.ideal.job.scheduler.core.socket.handler;

import cn.sh.ideal.job.scheduler.core.socket.SocketExecutor;

import javax.annotation.Nonnull;

/**
 * 消息处理器
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface MessageHandler {

  void execute(@Nonnull SocketExecutor executor, @Nonnull String messagePayload);
}
