package com.zzsong.job.common.worker;

import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.transfer.Res;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface RemoteTaskWorker extends TaskWorker {
  /**
   * 任务执行完成回调
   *
   * @param callback 回调消息
   * @author 宋志宗
   * @date 2020/8/22 23:47
   */
  Mono<Res<Void>> taskCallback(@Nonnull TaskCallback callback);
}
