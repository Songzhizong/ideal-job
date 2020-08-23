package cn.sh.ideal.job.common.message;

import cn.sh.ideal.job.common.ParseException;
import cn.sh.ideal.job.common.utils.JsonUtils;

import javax.annotation.Nonnull;

/**
 * 心跳消息
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public final class HeartbeatMessage extends SocketMessage {

  private HeartbeatMessage(@Nonnull String messageType, @Nonnull String payload) {
    super(messageType, payload);
  }

  public static HeartbeatMessage createInstance() {
    return new HeartbeatMessage(MessageType.HEARTBEAT.getCode(), "");
  }

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static HeartbeatMessage parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, HeartbeatMessage.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
