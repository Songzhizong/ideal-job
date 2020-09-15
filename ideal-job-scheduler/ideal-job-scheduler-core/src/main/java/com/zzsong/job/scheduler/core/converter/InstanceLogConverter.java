package com.zzsong.job.scheduler.core.converter;

import com.zzsong.job.common.message.payload.InstanceLogReport;
import com.zzsong.job.scheduler.core.pojo.InstanceLog;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/15
 */
@SuppressWarnings("DuplicatedCode")
public class InstanceLogConverter {

  public static InstanceLog fromInstanceLogReport(@Nonnull InstanceLogReport report) {
    InstanceLog instanceLog = new InstanceLog();
    instanceLog.setLogId(report.getLogId());
    instanceLog.setInstanceId(report.getInstanceId());
    instanceLog.setLogTime(report.getLogTime());
    instanceLog.setHandler(report.getHandler());
    instanceLog.setMessage(report.getMessage());
    return instanceLog;
  }
}
