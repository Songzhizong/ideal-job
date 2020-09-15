package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.scheduler.core.admin.storage.InstanceLogStorage;
import com.zzsong.job.scheduler.core.pojo.InstanceLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/15
 */
@Service
public class InstanceLogService {
  @Nonnull
  private final InstanceLogStorage instanceLogStorage;

  public InstanceLogService(@Nonnull InstanceLogStorage instanceLogStorage) {
    this.instanceLogStorage = instanceLogStorage;
  }


  public Mono<InstanceLog> save(@Nonnull InstanceLog instanceLog) {
    return instanceLogStorage.save(instanceLog);
  }

  /**
   * 通过任务实例ID获取日志列表
   *
   * @param instanceId 任务实例ID
   * @return 日志列表
   */
  public Mono<List<InstanceLog>> loadByInstanceId(long instanceId) {
    return instanceLogStorage.loadByInstanceId(instanceId);
  }

  /**
   * 删除指定时间之前的日志记录
   *
   * @param time 时间
   * @return 删除条数
   */
  public Mono<Integer> deleteAllByLogTimeLessThan(@Nonnull LocalDateTime time) {
    return instanceLogStorage.deleteAllByLogTimeLessThan(time);
  }
}
