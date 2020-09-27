package com.zzsong.job.scheduler.core.admin.storage;

import com.zzsong.job.scheduler.core.pojo.InstanceLog;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实例日志存储抽象
 *
 * @author 宋志宗 on 2020/9/15
 */
public interface InstanceLogStorage {

  Mono<InstanceLog> save(@Nonnull InstanceLog instanceLog);

  /**
   * 通过任务实例ID获取日志列表
   *
   * @param instanceId 任务实例ID
   * @return 日志列表
   */
  Mono<List<InstanceLog>> loadByInstanceId(long instanceId);

  /**
   * 删除指定时间之前的日志记录
   *
   * @param time 时间
   * @return 删除条数
   */
  Mono<Integer> deleteAllByLogTimeLessThan(@Nonnull LocalDateTime time);
}
