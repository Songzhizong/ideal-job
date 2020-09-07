package com.zzsong.job.scheduler.core.dispatch.handler;

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
   * @param executeParam 执行参数
   * @author 宋志宗
   * @date 2020/8/28 10:23 下午
   */
  @Nonnull
  Mono<Boolean> execute(@Nonnull LbServer lbServer,
                        @Nonnull JobInstance instance,
                        @Nonnull JobView jobView,
                        @Nonnull Object executeParam);

  /**
   * 解析执行参数
   *
   * @param executeParam 执行参数字符串
   * @return 解析结果
   * @author 宋志宗
   * @date 2020/9/7
   */
  @Nonnull
  Object parseExecuteParam(@Nonnull String executeParam) throws Exception;

  /**
   * 选取服务列表
   *
   * @param jobView      任务信息
   * @param executeParam 执行参数
   * @return 服务列表, 至少应返回1个, 否则抛出异常
   * @author 宋志宗
   * @date 2020/9/7
   */
  @Nonnull
  Mono<List<? extends LbServer>> chooseWorkers(@Nonnull JobView jobView,
                                               @Nonnull Object executeParam);
}
