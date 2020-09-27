package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.scheduler.core.pojo.JobView;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/10
 */
@Getter
@Setter
public class DispatchData {
  @Nonnull
  private JobView jobView;
  @Nonnull
  private TriggerTypeEnum triggerType;
  @Nullable
  private String customExecuteParam;
}
