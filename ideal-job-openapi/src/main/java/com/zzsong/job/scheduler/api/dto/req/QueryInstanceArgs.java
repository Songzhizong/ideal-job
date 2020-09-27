package com.zzsong.job.scheduler.api.dto.req;

import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.transfer.Range;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/9/9
 */
@Getter
@Setter
public class QueryInstanceArgs {
  /**
   * 父实例ID
   */
  @Nullable
  private Long parentId;
  /**
   * Job Id
   */
  @Nullable
  private Long jobId;
  /**
   * 执行器Id
   */
  @Nullable
  private Long executorId;

  /**
   * 执行状态
   */
  @Nullable
  private HandleStatusEnum handleStatus;

  /**
   * 调度时间区间
   */
  @Nullable
  private Range<LocalDateTime> dispatchTimeRange;
}
