package com.zzsong.job.scheduler.core.dispatcher;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.core.pojo.JobView;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 调度器接口
 *
 * @author 宋志宗 on 2020/9/9
 */
public interface JobDispatcher {

  /**
   * 执行任务调度
   *
   * @param jobView            job信息
   * @param triggerType        触发类型
   * @param customExecuteParam 自定义参数
   * @return 调用结果
   */
  Mono<Res<Void>> dispatch(@Nonnull JobView jobView,
                           @Nonnull TriggerTypeEnum triggerType,
                           @Nullable String customExecuteParam);
}
