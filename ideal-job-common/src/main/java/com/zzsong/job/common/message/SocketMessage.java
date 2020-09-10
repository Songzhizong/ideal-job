package com.zzsong.job.common.message;

import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * @author 宋志宗 on 2020/8/20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocketMessage {
  /**
   * 消息唯一ID
   */
  private String messageId = UUID.randomUUID().toString();
  /**
   * 消息类型
   */
  @Nonnull
  private String messageType;
  /**
   * 消息主体
   */
  @Nonnull
  private String payload = "";

  public SocketMessage(@Nonnull String messageType, @Nonnull String payload) {
    this.messageType = messageType;
    this.payload = payload;
  }

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static SocketMessage parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, SocketMessage.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
