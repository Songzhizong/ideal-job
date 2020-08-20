package cn.sh.ideal.nj.cmpt.job.common.pojo;

import cn.sh.ideal.nj.cmpt.job.common.utils.JsonUtils;

import javax.annotation.Nonnull;

/**
 * 心跳消息
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public final class HeartbeatMessage extends SocketMessage {
  public static final HeartbeatMessage INSTANCE = new HeartbeatMessage(Type.HEARTBEAT.getCode(), "");
  private static final String JSON_STRING = JsonUtils.toJsonString(INSTANCE);

  private HeartbeatMessage(int messageType, String payload) {
    super(messageType, payload);
  }

  @Override
  public int getMessageType() {
    return super.getMessageType();
  }

  @Nonnull
  @Override
  public String getPayload() {
    return super.getPayload();
  }

  @Override
  public void setMessageType(int messageType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPayload(@Nonnull String payload) {
    throw new UnsupportedOperationException();
  }

  public String jsonString() {
    return JSON_STRING;
  }
}
