package cn.sh.ideal.job.worker.handler.impl;

import cn.sh.ideal.job.worker.handler.IJobHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
public class MethodJobHandler implements IJobHandler {
  private final Object target;
  private final Method method;
  private final boolean hasParam;

  public MethodJobHandler(Object target,
                          Method method,
                          boolean hasParam) {
    this.target = target;
    this.method = method;
    this.hasParam = hasParam;
  }

  @Nullable
  @Override
  public Object execute(@Nonnull String param) throws Exception {
    if (hasParam) {
      return method.invoke(target, param);
    } else {
      return method.invoke(target);
    }
  }
}
