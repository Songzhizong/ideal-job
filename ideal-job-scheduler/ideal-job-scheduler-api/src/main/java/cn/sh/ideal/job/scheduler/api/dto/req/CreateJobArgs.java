package cn.sh.ideal.job.scheduler.api.dto.req;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class CreateJobArgs {
  /**
   * 创建后是否自动运行, 默认否
   */
  @Nullable
  private Boolean autoStart;
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
  private String executorParam;
  /**
   * 路由策略,默认轮询
   */
  @Nullable
  private LbStrategyEnum routeStrategy;
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
   * 任务描述
   */
  @Nullable
  private String description;
  /**
   * 告警邮件地址
   */
  @Nullable
  private String alarmEmail;

  // ---------------------------- 以下为扩展查询字段, 适用于各种场景的查询需求

  /**
   * 所属应用, 用于多应用隔离
   */
  @Nullable
  private String application;
  /**
   * 所属租户, 用于saas系统租户隔离
   */
  @Nullable
  private String tenantId;
  /**
   * 所属业务
   */
  @Nullable
  private String bizType;
  /**
   * 自定义标签
   */
  @Nullable
  private String customTag;
  /**
   * 业务id
   */
  @Nullable
  private String businessId;
}
