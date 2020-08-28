package cn.sh.ideal.job.scheduler.core.dispatch.handler;

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
public final class ExecuteHandlerFactory {

  private static final Map<ExecuteTypeEnum, ExecuteHandler> handlerMapper = new HashMap<>();

  public static void register(@Nonnull ExecuteTypeEnum type,
                              @Nonnull ExecuteHandler handler) {
    handlerMapper.put(type, handler);
  }

  @Nullable
  public static ExecuteHandler getHandler(@Nonnull ExecuteTypeEnum type) {
    return handlerMapper.get(type);
  }


}
