package cn.sh.ideal.job.executor.core.handler;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
public interface IJobHandler {

  void execute(@Nonnull String param) throws Exception;

  default void init() throws Exception {
    // do something
  }

  default void destroy() throws Exception {
    // do something
  }
}
