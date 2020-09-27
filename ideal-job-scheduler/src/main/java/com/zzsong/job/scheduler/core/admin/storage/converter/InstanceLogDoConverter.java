package com.zzsong.job.scheduler.core.admin.storage.converter;

import com.zzsong.job.scheduler.core.admin.storage.db.entity.InstanceLogDo;
import com.zzsong.job.scheduler.core.pojo.InstanceLog;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/15
 */
@SuppressWarnings("DuplicatedCode")
public class InstanceLogDoConverter {

  @Nonnull
  public static InstanceLogDo fromInstanceLog(@Nonnull InstanceLog instanceLog) {
    InstanceLogDo instanceLogDo = new InstanceLogDo();
    instanceLogDo.setLogId(instanceLog.getLogId());
    instanceLogDo.setInstanceId(instanceLog.getInstanceId());
    instanceLogDo.setLogTime(instanceLog.getLogTime());
    instanceLogDo.setHandler(instanceLog.getHandler());
    instanceLogDo.setMessage(instanceLog.getMessage());
    return instanceLogDo;
  }

  @Nonnull
  public static InstanceLog toInstanceLog(@Nonnull InstanceLogDo instanceLogDo) {
    InstanceLog instanceLog = new InstanceLog();
    instanceLog.setLogId(instanceLogDo.getLogId());
    instanceLog.setInstanceId(instanceLogDo.getInstanceId());
    instanceLog.setLogTime(instanceLogDo.getLogTime());
    instanceLog.setHandler(instanceLogDo.getHandler());
    instanceLog.setMessage(instanceLogDo.getMessage());
    return instanceLog;
  }
}
