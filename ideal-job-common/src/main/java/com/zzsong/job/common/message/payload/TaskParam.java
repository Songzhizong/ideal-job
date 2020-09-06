package com.zzsong.job.common.message.payload;

import com.zzsong.job.common.constants.BlockStrategyEnum;
import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.message.MessageType;
import com.zzsong.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class TaskParam {
  public static String typeCode = MessageType.EXECUTE_JOB.getCode();
  /**
   * 任务id
   */
  @Nonnull
  private String jobId = "";
  /**
   * 任务实例ID
   */
  private long instanceId;
  /**
   * 执行处理器
   */
  private String executorHandler;
  /**
   * 执行参数
   */
  private String executeParam;
  /**
   * 阻塞策略,{@link BlockStrategyEnum}
   */
  @Nonnull
  private String blockStrategy;

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static TaskParam parseMessage(@Nonnull String message) throws ParseException {
    try {
      return JsonUtils.parseJson(message, TaskParam.class);
    } catch (Exception exception) {
      throw new ParseException(exception);
    }
  }
}
