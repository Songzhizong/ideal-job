package cn.sh.ideal.job.scheduler.core.admin.controller;

import cn.sh.ideal.job.common.transfer.Paging;
import cn.sh.ideal.job.common.transfer.Res;
import cn.sh.ideal.job.scheduler.api.client.ExecutorClient;
import cn.sh.ideal.job.scheduler.api.dto.req.CreateExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.QueryExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor;
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  public Res<Long> create(@Validated @RequestBody
                          @Nonnull CreateExecutorArgs args) {
    final JobExecutor executor = service.create(args);
    final Long executorId = executor.getExecutorId();
    return Res.data(executorId);
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
  public Res<Void> update(@Validated @RequestBody
                          @Nonnull UpdateExecutorArgs args) {
    service.update(args);
    return Res.success();
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
  public Res<Void> delete(@Nonnull Long executorId) {
    service.delete(executorId);
    return Res.success();
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
