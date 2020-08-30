package cn.sh.ideal.job.scheduler.core.socket.handler;

import cn.sh.ideal.job.common.message.SocketMessage;
import cn.sh.ideal.job.scheduler.core.socket.SocketTaskExecutor;

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
  void execute(@Nonnull SocketTaskExecutor executor,
               @Nonnull SocketMessage socketMessage);
}
