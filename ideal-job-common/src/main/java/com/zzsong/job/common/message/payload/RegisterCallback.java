package com.zzsong.job.common.message.payload;

import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.message.MessageType;
import com.zzsong.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/20
 */
@Getter
@Setter
public class RegisterCallback {
  public static String typeCode = MessageType.REGISTER_CALLBACK.getCode();
  /**
   * 对应的注册消息ID
   */
  private String messageId;
  /**
   * 是否注册成功
   */
  private boolean success;
  /**
   * 提示信息
   */
  private String message;


  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static RegisterCallback parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, RegisterCallback.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
