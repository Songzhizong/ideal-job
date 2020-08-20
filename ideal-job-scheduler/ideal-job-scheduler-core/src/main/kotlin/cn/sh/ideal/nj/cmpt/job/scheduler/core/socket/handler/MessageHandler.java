package cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler;

import cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.SocketExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 消息处理器
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface MessageHandler {

  void execute(@Nonnull SocketExecutor executor, @Nonnull String messagePayload);
}
