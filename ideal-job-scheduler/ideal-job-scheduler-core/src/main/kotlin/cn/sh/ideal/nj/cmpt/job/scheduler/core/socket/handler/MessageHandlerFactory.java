package cn.sh.ideal.nj.cmpt.job.scheduler.core.socket.handler;

import cn.sh.ideal.nj.cmpt.job.common.pojo.SocketMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public final class MessageHandlerFactory {
  private static final Map<SocketMessage.Type, MessageHandler> handlerMapper = new HashMap<>();

  public static void register(@Nonnull SocketMessage.Type type,
                              @Nonnull MessageHandler handler) {
    handlerMapper.put(type, handler);
  }

  @Nullable
  public static MessageHandler getHandler(@Nonnull SocketMessage.Type type) {
    return handlerMapper.get(type);
  }
}
