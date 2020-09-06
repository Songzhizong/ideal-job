package com.zzsong.job.scheduler.core.dispatch.handler;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import com.zzsong.job.scheduler.core.pojo.JobView;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
public interface ExecuteHandler {

  /**
   * 调度执行任务
   *
   * @param jobView      任务信息
   * @param triggerType  触发类型
   * @param executeParam 执行参数
   * @author 宋志宗
   * @date 2020/8/28 10:23 下午
   */
  Mono<Boolean> execute(@Nonnull JobInstance instance,
                        @Nonnull JobView jobView,
                        @Nonnull TriggerTypeEnum triggerType,
                        @Nonnull Object executeParam);


  Mono<Object> parseExecuteParam(@Nonnull String executeParam);

  List<? extends LbServer> chooseWorkers(@Nonnull JobView jobView,
                                         @Nonnull Object executeParam);
}
