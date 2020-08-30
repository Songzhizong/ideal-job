package cn.sh.ideal.job.scheduler.core.admin.controller;

import cn.sh.ideal.job.common.transfer.Paging;
import cn.sh.ideal.job.scheduler.api.dto.req.CreateJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.QueryJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateJobArgs;
import cn.sh.ideal.job.scheduler.api.dto.rsp.JobInfoRsp;
import cn.sh.ideal.job.scheduler.core.admin.service.JobService;
import cn.sh.ideal.job.common.transfer.Res;
import cn.sh.ideal.job.scheduler.api.client.JobClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 任务管理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Validated
@RestController
@RequestMapping("/job")
public class JobController implements JobClient {

  private final JobService jobService;

  public JobController(JobService jobService) {
    this.jobService = jobService;
  }

  /**
   * 新建任务
   *
   * @param createJobArgs 新增任务请求参数
   * @return 任务id
   * @author 宋志宗
   * @date 2020/8/26 7:36 下午
   */
  @Nonnull
  @Override
  @PostMapping("/create")
  public Res<Long> create(@Validated @RequestBody
                          @Nonnull CreateJobArgs createJobArgs) {
    return jobService.createJob(createJobArgs);
  }

  /**
   * 更新任务信息
   *
   * @param updateJobArgs 更新参数
   * @return 更新结果
   * @author 宋志宗
   * @date 2020/8/26 8:48 下午
   */
  @Nonnull
  @Override
  @PostMapping("/update")
  public Res<Void> update(@Validated @RequestBody
                          @Nonnull UpdateJobArgs updateJobArgs) {
    return jobService.updateJob(updateJobArgs);
  }

  /**
   * 移除任务
   *
   * @param jobId 任务id
   * @return 移除结果
   * @author 宋志宗
   * @date 2020/8/26 8:49 下午
   */
  @Nonnull
  @Override
  @PostMapping("/remove")
  public Res<Void> remove(@Nonnull Long jobId) {
    return jobService.removeJob(jobId);
  }

  /**
   * 查询任务信息
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 任务信息列表
   * @author 宋志宗
   * @date 2020/8/26 8:51 下午
   */
  @Nonnull
  @Override
  @PostMapping("/query")
  public Res<List<JobInfoRsp>> query(@RequestBody @Nullable QueryJobArgs args,
                                     @Nullable Paging paging) {
    if (args == null) {
      args = new QueryJobArgs();
    }
    if (paging == null) {
      paging = Paging.of(1, 10);
    }
    paging.cleanOrders();
    paging.descBy("jobId");
    return jobService.query(args, paging);
  }

  /**
   * 启用任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  @Nonnull
  @Override
  @PostMapping("/enable")
  public Res<Void> enable(@Nonnull Long jobId) {
    return jobService.enableJob(jobId);
  }

  /**
   * 停用任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  @Nonnull
  @Override
  @PostMapping("/disable")
  public Res<Void> disable(@Nonnull Long jobId) {
    return jobService.disableJob(jobId);
  }

  /**
   * 触发任务
   *
   * @param jobId              任务id
   * @param executeParam 执行参数, 为<code>null</code>空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:18 下午
   */
  @Nonnull
  @Override
  @PostMapping("/trigger")
  public Res<Void> trigger(@Nonnull Long jobId, @Nullable String executeParam) {
    jobService.trigger(jobId, executeParam);
    return Res.success();
  }
}
