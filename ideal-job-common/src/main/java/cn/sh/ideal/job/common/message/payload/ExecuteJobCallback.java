package cn.sh.ideal.job.common.message.payload;

import cn.sh.ideal.job.common.ParseException;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class ExecuteJobCallback {
  /**
   * 回调序列生成器
   */
  private static final AtomicLong SEQUENCE = new AtomicLong(0);
  public static final String typeCode = MessageType.EXECUTE_JOB_CALLBACK.getCode();

  /**
   * 任务执行前和任务执行结束 执行器都会执行一次回调分别用于将任务触发标记为执行中和执行完成状态,
   * 如果任务执行太快可能会导致执行完成的回调和执行开始的回调同时到达服务端甚至先一步到达服务端,
   * 这种情况可能导致任务执行状态出现错乱.
   * 为了解决这一问题, 每一次回调都调用一次SEQUENCE.incrementAndGet()获取执行序列, 通过序列便可轻松判断回调掉先后顺序
   */
  private long sequence;
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

  public void initSequence() {
    this.sequence = SEQUENCE.incrementAndGet();
  }


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
