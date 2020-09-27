package com.zzsong.job.scheduler.core.admin.storage.param;

import com.zzsong.job.common.constants.HandleStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 任务执行结果
 *
 * @author 宋志宗 on 2020/9/6
 */
@Getter
@Setter
public class TaskResult {
  private Long instanceId;
  private long handleTime;
  private long finishedTime;
  private HandleStatusEnum handleStatus;
  private String result;
  private int sequence;
  private LocalDateTime updateTime;
}
