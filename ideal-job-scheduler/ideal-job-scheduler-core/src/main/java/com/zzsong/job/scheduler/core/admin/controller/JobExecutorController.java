package com.zzsong.job.scheduler.core.admin.controller;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.ExecutorClient;
import com.zzsong.job.scheduler.api.dto.req.CreateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.api.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.service.JobExecutorService;
import com.zzsong.job.scheduler.core.conf.ExceptionHandler;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@RestController
@RequestMapping("/executor")
public class JobExecutorController implements ExecutorClient {
  private final JobExecutorService service;

  public JobExecutorController(JobExecutorService service) {
    this.service = service;
  }

  /**
   * 新增执行器
   *
   * @param args 新建参数
   * @return 执行器ID
   * @author 宋志宗
   * @date 2020/8/26 23:41
   */
  @Nonnull
  @Override
  @PostMapping("/create")
  public Mono<Res<JobWorker>> create(@RequestBody @Nonnull CreateWorkerArgs args) {
    return Mono.just(args)
        .map(CreateWorkerArgs::checkArgs)
        .flatMap(service::create)
        .map(Res::data)
        .onErrorResume(ExceptionHandler::disposeResult);
  }

  /**
   * 更新执行器信息
   *
   * @param args 更新参数
   * @return 更新结果
   * @author 宋志宗
   * @date 2020/8/26 23:42
   */
  @Nonnull
  @Override
  @PostMapping("/update")
  public Mono<Res<JobWorker>> update(@RequestBody @Nonnull UpdateWorkerArgs args) {
    return Mono.just(args)
        .map(UpdateWorkerArgs::checkArgs)
        .flatMap(service::update)
        .map(Res::data)
        .onErrorResume(ExceptionHandler::disposeResult);
  }

  /**
   * 删除执行器
   *
   * @param workerId 执行器ID
   * @return 删除结果
   * @author 宋志宗
   * @date 2020/8/26 23:43
   */
  @Nonnull
  @Override
  @PostMapping("/delete")
  public Mono<Res<Integer>> delete(long workerId) {
    if (workerId < 1) {
      return Mono.error(new VisibleException(("执行器ID不合法")));
    }
    return service.delete(workerId).map(Res::data)
        .onErrorResume(ExceptionHandler::disposeResult);
  }

  /**
   * 查询执行器列表
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 执行器信息列表
   * @author 宋志宗
   * @date 2020/8/26 23:45
   */
  @Nonnull
  @Override
  @PostMapping("/query")
  public Mono<Res<List<ExecutorInfoRsp>>> query(@RequestBody @Nonnull QueryWorkerArgs args,
                                                @Nonnull Paging paging) {
    paging.cleanOrders();
    paging.descBy("executorId");
    return service.query(args, paging)
        .onErrorResume(ExceptionHandler::disposeResult);
  }
}
