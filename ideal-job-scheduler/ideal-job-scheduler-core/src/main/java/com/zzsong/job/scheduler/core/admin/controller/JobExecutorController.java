package com.zzsong.job.scheduler.core.admin.controller;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.ExecutorClient;
import com.zzsong.job.scheduler.api.dto.req.CreateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.core.admin.db.entity.JobExecutorDo;
import com.zzsong.job.scheduler.core.admin.service.JobExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@RestController
@RequestMapping("/executor")
public class JobExecutorController implements ExecutorClient {
    private static final Logger log = LoggerFactory.getLogger(JobExecutorController.class);
    private final Scheduler blockScheduler;
    private final JobExecutorService service;

    public JobExecutorController(Scheduler blockScheduler,
                                 JobExecutorService service) {
        this.blockScheduler = blockScheduler;
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
    public Mono<Res<Long>> create(@RequestBody @Nonnull CreateExecutorArgs args) {
        return Mono.just(args).publishOn(blockScheduler)
                .map(a -> {
                    JobExecutorDo jobExecutor = service.create(a.checkArgs());
                    return Res.data(jobExecutor.getExecutorId());
                })
                .onErrorResume(throwable -> {
                    log.info("exception: {}", throwable.getMessage());
                    return Mono.just(Res.err(throwable.getMessage()));
                });
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
    public Mono<Res<Void>> update(@RequestBody @Nonnull UpdateExecutorArgs args) {
        return Mono.just(args).publishOn(blockScheduler)
                .map(a -> {
                    service.update(a.checkArgs());
                    return Res.<Void>success();
                })
                .onErrorResume(throwable -> {
                    log.info("exception: {}", throwable.getMessage());
                    return Mono.just(Res.err(throwable.getMessage()));
                });
    }

    /**
     * 删除执行器
     *
     * @param executorId 执行器ID
     * @return 删除结果
     * @author 宋志宗
     * @date 2020/8/26 23:43
     */
    @Nonnull
    @Override
    @PostMapping("/delete")
    public Mono<Res<Void>> delete(long executorId) {
        return Mono.just(executorId).publishOn(blockScheduler)
                .map(id -> {
                    if (id < 1) {
                        throw new VisibleException("执行器id不合法");
                    }
                    service.delete(id);
                    return Res.<Void>success();
                })
                .onErrorResume(throwable -> {
                    log.info("exception: {}", throwable.getMessage());
                    return Mono.just(Res.err(throwable.getMessage()));
                });
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
    public Mono<Res<List<ExecutorInfoRsp>>> query(@RequestBody @Nonnull QueryExecutorArgs args,
                                                  @Nonnull Paging paging) {
        paging.cleanOrders();
        paging.descBy("executorId");
        return Mono.just(args).publishOn(blockScheduler)
                .map(e -> service.query(args, paging))
                .onErrorResume(throwable -> {
                    log.info("exception: {}", throwable.getMessage());
                    return Mono.just(Res.err(throwable.getMessage()));
                });
    }
}
