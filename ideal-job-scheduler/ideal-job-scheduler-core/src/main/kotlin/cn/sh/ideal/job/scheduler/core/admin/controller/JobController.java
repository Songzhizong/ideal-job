package cn.sh.ideal.job.scheduler.core.admin.controller;

import cn.sh.ideal.job.common.constants.DBDefaults;
import cn.sh.ideal.job.scheduler.core.admin.service.JobService;
import cn.sh.ideal.job.common.res.Res;
import cn.sh.ideal.job.scheduler.api.client.JobApi;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Validated
@RestController
@RequestMapping("/job")
public class JobController implements JobApi {

  private final JobService jobService;

  public JobController(JobService jobService) {
    this.jobService = jobService;
  }

  @Nonnull
  @Override
  @PostMapping("/start")
  public Res<Void> start(@Nonnull Long jobId) {
    return Res.success();
  }

  @Nonnull
  @Override
  @PostMapping("/stop")
  public Res<Void> stop(@Nonnull Long jobId) {
    return Res.success();
  }

  @Nonnull
  @Override
  @PostMapping("/trigger")
  public Res<Void> trigger(@Nonnull Long jobId, @Nullable String executorParam) {
    jobService.trigger(jobId, executorParam);
    return Res.success();
  }
}
