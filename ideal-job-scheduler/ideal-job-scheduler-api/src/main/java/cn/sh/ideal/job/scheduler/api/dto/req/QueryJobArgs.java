package cn.sh.ideal.job.scheduler.api.dto.req;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class QueryJobArgs {
  /**
   * 所属执行器Id
   */
  @Nullable
  private Long executorId;
  /**
   * 任务名称
   */
  @Nullable
  private String jobName;
  /**
   * JobHandler
   */
  @Nullable
  private String executorHandler;
  /**
   * 任务状态, 0-停止，1-运行， 其他-全部
   */
  @Nullable
  private Integer jobStatus;


  // ---------------------------- 以下为扩展查询字段, 适用于各种业务场景的查询需求
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
