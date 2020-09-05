package com.zzsong.job.scheduler.core.admin.controller;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import com.zzsong.job.scheduler.core.admin.service.JobService;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.JobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 任务管理
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@RestController
@RequestMapping("/job")
public class JobController implements JobClient {
  private static final Logger log = LoggerFactory.getLogger(JobController.class);

  private final JobService jobService;
  private final Scheduler blockScheduler;

  public JobController(JobService jobService,
                       Scheduler blockScheduler) {
    this.jobService = jobService;
    this.blockScheduler = blockScheduler;
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
  public Mono<Res<Long>> create(@RequestBody @Nonnull CreateJobArgs createJobArgs) {
    return Mono.just(createJobArgs).publishOn(blockScheduler)
        .map(a -> {
          long jobId = jobService.createJob(a.checkArgs());
          return Res.data(jobId);
        })
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
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
  public Mono<Res<Void>> update(@Validated @RequestBody
                                @Nonnull UpdateJobArgs updateJobArgs) {
    return Mono.just(updateJobArgs).publishOn(blockScheduler)
        .map(a -> {
          jobService.updateJob(a.checkArgs());
          return Res.<Void>success();
        })
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
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
  public Mono<Res<Void>> remove(long jobId) {
    return Mono.just(jobId).publishOn(blockScheduler)
        .map(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
          jobService.removeJob(id);
          return Res.<Void>success();
        })
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
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
  public Mono<Res<List<JobInfoRsp>>> query(@RequestBody @Nonnull QueryJobArgs args,
                                           @Nonnull Paging paging) {
    paging.cleanOrders();
    paging.descBy("jobId");
    return Mono.just(args).publishOn(blockScheduler)
        .map(e -> jobService.query(args, paging))
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
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
  public Mono<Res<Void>> enable(long jobId) {
    return Mono.just(jobId).publishOn(blockScheduler)
        .map(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
          jobService.enableJob(id);
          return Res.<Void>success();
        })
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
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
  public Mono<Res<Void>> disable(long jobId) {
    return Mono.just(jobId).publishOn(blockScheduler)
        .map(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
          jobService.disableJob(id);
          return Res.<Void>success();
        })
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
  }

  /**
   * 触发任务
   *
   * @param jobId        任务id
   * @param executeParam 执行参数, 为<code>null</code>空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:18 下午
   */
  @Nonnull
  @Override
  @PostMapping("/trigger")
  public Mono<Res<Void>> trigger(long jobId, @Nullable String executeParam) {
    jobService.triggerJob(jobId, executeParam);

    return Mono.just(jobId).publishOn(blockScheduler)
        .map(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
          jobService.triggerJob(id, executeParam);
          return Res.<Void>success();
        })
        .onErrorResume(throwable -> {
          log.info("exception: {}", throwable.getMessage());
          return Mono.just(Res.err(throwable.getMessage()));
        });
  }
}
