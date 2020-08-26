package cn.sh.ideal.job.common.message.payload;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
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
public class ExecuteJobParam {
  public static String typeCode = MessageType.EXECUTE_JOB.getCode();
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
   * 执行处理器
   */
  private String executorHandler;
  /**
   * 执行参数
   */
  private String executorParams;
  /**
   * 阻塞策略,{@link BlockStrategyEnum}
   */
  private int blockStrategy;

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static ExecuteJobParam parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, ExecuteJobParam.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
