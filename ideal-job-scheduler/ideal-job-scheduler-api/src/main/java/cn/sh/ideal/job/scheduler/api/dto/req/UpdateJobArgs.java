package cn.sh.ideal.job.scheduler.api.dto.req;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.RouteStrategyEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class UpdateJobArgs {

  /**
   * 任务ID
   */
  @Nonnull
  @NotNull(message = "jobId不能为空")
  private Long jobId;
  /**
   * 所属执行器Id
   */
  @Nonnull
  @NotNull(message = "所属执行器id不能为空")
  private Long executorId;
  /**
   * JobHandler
   */
  @Nullable
  private String executorHandler;
  /**
   * 执行参数
   */
  @Nullable
  private String executeParam;
  /**
   * 路由策略,默认轮询
   */
  @Nullable
  private RouteStrategyEnum routeStrategy;
  /**
   * 阻塞策略, 默认串行执行
   */
  @Nullable
  private BlockStrategyEnum blockStrategy;
  /**
   * 任务执行CRON
   */
  @Nullable
  private String cron;
  /**
   * 失败重试次数, 默认不重试
   */
  @Nullable
  private Integer retryCount;
  /**
   * 任务名称
   */
  @Nullable
  private String jobName;
  /**
   * 告警邮件地址
   */
  @Nullable
  private String alarmEmail;
}
