package cn.sh.ideal.job.executor.core.handler.impl;

import cn.sh.ideal.job.executor.core.handler.IJobHandler;

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
  @Nullable
  private final Method initMethod;
  @Nullable
  private final Method destroyMethod;

  public MethodJobHandler(Object target,
                          Method method,
                          boolean hasParam,
                          @Nullable Method initMethod,
                          @Nullable Method destroyMethod) {
    this.target = target;
    this.method = method;
    this.hasParam = hasParam;
    this.initMethod = initMethod;
    this.destroyMethod = destroyMethod;
  }

  @Override
  public void execute(@Nonnull String param) throws Exception {
    if (hasParam) {
      method.invoke(target, param);
    } else {
      method.invoke(target);
    }
  }

  @Override
  public void init() throws Exception {
    if (initMethod != null) {
      initMethod.invoke(target);
    }
  }

  @Override
  public void destroy() throws Exception {
    if (destroyMethod != null) {
      destroyMethod.invoke(target);
    }
  }
}
