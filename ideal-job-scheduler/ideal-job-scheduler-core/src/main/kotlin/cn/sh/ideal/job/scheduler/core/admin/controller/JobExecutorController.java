package cn.sh.ideal.job.scheduler.core.admin.controller;

import cn.sh.ideal.job.common.transfer.Paging;
import cn.sh.ideal.job.common.transfer.Res;
import cn.sh.ideal.job.scheduler.api.client.ExecutorClient;
import cn.sh.ideal.job.scheduler.api.dto.req.CreateExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.QueryExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Validated
@RestController
@RequestMapping("/executor")
public class JobExecutorController implements ExecutorClient {
  private final JobExecutorService service;

  public JobExecutorController(JobExecutorService service) {
    this.service = service;
  }


  @Nonnull
  @Override
  public Res<Long> create(@Validated @RequestBody
                          @Nonnull CreateExecutorArgs args) {
    long executorId = service.create(args);
    return Res.data(executorId);
  }

  @Nonnull
  @Override
  public Res<Void> update(@Validated @RequestBody
                          @Nonnull UpdateExecutorArgs args) {
    service.update(args);
    return Res.success();
  }

  @Nonnull
  @Override
  public Res<Void> delete(@Nonnull Long executorId) {
    service.delete(executorId);
    return Res.success();
  }

  @Nonnull
  @Override
  public Res<List<ExecutorInfoRsp>> query(@RequestBody @Nullable QueryExecutorArgs args,
                                          @Nullable Paging paging) {
    if (args == null) {
      args = new QueryExecutorArgs();
    }
    if (paging == null) {
      paging = Paging.of(1, 10);
    }
    paging.cleanOrders();
    paging.descBy("executorId");
    return service.query(args, paging);
  }
}
