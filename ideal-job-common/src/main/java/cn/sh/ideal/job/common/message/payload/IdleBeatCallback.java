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
public class IdleBeatCallback {
  public static String typeCode = MessageType.IDLE_BEAT_CALLBACK.getCode();

  @Nonnull
  private String jobId = "";

  private int idleLevel;

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static IdleBeatCallback parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, IdleBeatCallback.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
