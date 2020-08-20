package cn.sh.ideal.nj.cmpt.job.common.pojo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public final class HeartbeatMessage extends SocketMessage {
  public static final HeartbeatMessage INSTANCE = new HeartbeatMessage(Type.HEARTBEAT.getCode(), "");

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
}
