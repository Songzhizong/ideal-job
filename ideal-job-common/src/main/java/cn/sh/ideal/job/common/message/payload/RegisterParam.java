package cn.sh.ideal.job.common.message.payload;

import cn.sh.ideal.job.common.ParseException;
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
public class RegisterParam {
  public static String typeCode = MessageType.REGISTER.getCode();
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

  public static RegisterParam parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, RegisterParam.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
