package cn.sh.ideal.job.scheduler.api.dto.req;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class QueryExecutorArgs {
  /**
   * 执行器AppName
   */
  @Nullable
  private String appName;
  /**
   * 执行器名称
   */
  @Nullable
  private String title;
}
