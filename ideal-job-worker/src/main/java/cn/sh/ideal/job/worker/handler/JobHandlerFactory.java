package cn.sh.ideal.job.worker.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
public final class JobHandlerFactory {
  private static final Map<String, IJobHandler> JOB_HANDLER_MAP = new HashMap<>();

  public static void register(@Nonnull String handlerName, @Nonnull IJobHandler jobHandler) {
    JOB_HANDLER_MAP.put(handlerName, jobHandler);
  }

  @Nullable
  public static IJobHandler get(@Nonnull String handlerName) {
    return JOB_HANDLER_MAP.get(handlerName);
  }
}
