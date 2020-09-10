package com.zzsong.job.common.message.payload;

import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.message.MessageType;
import com.zzsong.job.common.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginMessage {
  public static String typeCode = MessageType.REGISTER.getCode();
  @Nonnull
  private String appName;
  @Nonnull
  private String instanceId;
  /**
   * 鉴权token
   */
  private String accessToken;
  /**
   * 权重
   */
  private int weight = 1;

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static LoginMessage parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, LoginMessage.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
