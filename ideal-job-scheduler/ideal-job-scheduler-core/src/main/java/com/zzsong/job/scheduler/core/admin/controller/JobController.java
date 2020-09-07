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
import com.zzsong.job.scheduler.core.conf.ExceptionHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
  public Mono<Res<JobInfoRsp>> create(@RequestBody @Nonnull CreateJobArgs createJobArgs) {
    return Mono.just(createJobArgs)
        .doOnNext(CreateJobArgs::checkArgs)
        .flatMap(jobService::createJob)
        .map(Res::data)
        .onErrorResume(ExceptionHandler::resultException);
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
  public Mono<Res<JobInfoRsp>> update(@Validated @RequestBody
                                      @Nonnull UpdateJobArgs updateJobArgs) {
    return Mono.just(updateJobArgs)
        .doOnNext(UpdateJobArgs::checkArgs)
        .flatMap(jobService::updateJob)
        .map(Res::data)
        .onErrorResume(ExceptionHandler::resultException);

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
  @DeleteMapping("/remove/{jobId}")
  public Mono<Res<Void>> remove(@PathVariable("jobId") long jobId) {
    return Mono.just(jobId)
        .doOnNext(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
        })
        .flatMap(jobService::removeJob)
        .map(b -> Res.<Void>success())
        .onErrorResume(ExceptionHandler::resultException);
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
    paging.cleanOrders().descBy("jobId");
    return jobService.query(args, paging)
        .onErrorResume(ExceptionHandler::resultException);
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
  @PutMapping("/enable/{jobId}")
  public Mono<Res<Void>> enable(@PathVariable("jobId") long jobId) {
    return Mono.just(jobId)
        .doOnNext(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
        })
        .flatMap(jobService::enableJob)
        .map(b -> Res.<Void>success())
        .onErrorResume(ExceptionHandler::resultException);
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
  @PutMapping("/disable/{jobId}")
  public Mono<Res<Void>> disable(@PathVariable("jobId") long jobId) {
    return Mono.just(jobId)
        .doOnNext(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
        })
        .flatMap(jobService::disableJob)
        .map(b -> Res.<Void>success())
        .onErrorResume(ExceptionHandler::resultException);
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
  @PostMapping("/trigger/{jobId}")
  public Mono<Res<Void>> trigger(@PathVariable("jobId") long jobId,
                                 @Nullable String executeParam) {
    return Mono.just(jobId)
        .doOnNext(id -> {
          if (id < 1) {
            throw new VisibleException("任务id不合法");
          }
        })
        .flatMap(id -> jobService.triggerJob(id, executeParam))
        .map(b -> Res.<Void>success())
        .onErrorResume(ExceptionHandler::resultException);
  }
}
