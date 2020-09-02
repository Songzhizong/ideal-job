package cn.sh.ideal.job.scheduler.core.socket.weosocket.handler;

import cn.sh.ideal.job.common.message.MessageType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public final class MessageHandlerFactory {
  private static final Map<MessageType, MessageHandler> handlerMapper = new HashMap<>();

  public static void register(@Nonnull MessageType type,
                              @Nonnull MessageHandler handler) {
    handlerMapper.put(type, handler);
  }

  @Nullable
  public static MessageHandler getHandler(@Nonnull MessageType type) {
    return handlerMapper.get(type);
  }
}
