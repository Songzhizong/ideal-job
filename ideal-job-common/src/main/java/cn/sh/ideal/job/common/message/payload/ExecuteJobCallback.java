package cn.sh.ideal.job.common.message.payload;

import cn.sh.ideal.job.common.ParseException;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class ExecuteJobCallback {
  public static String typeCode = MessageType.EXECUTE_JOB_CALLBACK.getCode();
  /**
   * 任务id
   */
  @Nonnull
  private String jobId = "";
  /**
   * 触发日志id
   */
  private long triggerId;
  /**
   * 执行状态
   */
  private int handleStatus = -1;
  /**
   * 执行日志
   */
  @Nonnull
  private String handleMessage = "";
  /**
   * 执行耗时
   */
  @Nullable
  private Long timeConsuming;


  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static ExecuteJobCallback parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, ExecuteJobCallback.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
