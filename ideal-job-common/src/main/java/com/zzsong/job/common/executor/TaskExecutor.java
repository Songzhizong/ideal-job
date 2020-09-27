package com.zzsong.job.common.executor;

import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.transfer.Res;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/20
 */
public interface TaskExecutor extends LbServer {

  /**
   * 执行任务
   *
   * @param param 触发器参数
   * @author 宋志宗 on 2020/8/20 2:12 下午
   */
  Mono<Res<Void>> execute(@Nonnull TaskParam param);
}
