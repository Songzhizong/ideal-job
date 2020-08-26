package cn.sh.ideal.job.common.message.payload;

import cn.sh.ideal.job.common.exception.ParseException;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
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
