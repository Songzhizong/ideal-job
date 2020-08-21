package cn.sh.ideal.job.common.pojo.payload;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class ExecuteParam implements Serializable {
  private static final long serialVersionUID = -1L;
  /**
   * 任务id
   */
  private long jobId;

  /**
   * 执行处理器
   */
  private String executorHandler;

  /**
   * 执行参数
   */
  private String executorParams;
}
