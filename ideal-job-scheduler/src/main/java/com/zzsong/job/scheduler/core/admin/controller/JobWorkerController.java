package com.zzsong.job.scheduler.core.admin.controller;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.WorkerClient;
import com.zzsong.job.scheduler.api.dto.req.CreateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryWorkerArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateWorkerArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobWorkerRsp;
import com.zzsong.job.scheduler.core.admin.service.JobWorkerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 执行器管理
 *
 * @author 宋志宗 on 2020/8/20
 */
@Validated
@RestController
@RequestMapping("/worker")
public class JobWorkerController implements WorkerClient {
  private final JobWorkerService service;

  public JobWorkerController(JobWorkerService service) {
    this.service = service;
  }

  /**
   * 新增执行器
   * <pre>
   *   <b>请求示例:</b>
   *   POST http://{{host}}:{{port}}/{{gateway}}/worker/create
   *   Content-Type: application/json
   *
   *   {
   *     "appName": "SAMPLE-EXECUTOR",
   *     "title": "示例执行器"
   *   }
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success",
   *     "data": {
   *       "workerId": "134917723116273664",
   *       "appName": "SAMPLE-EXECUTOR",
   *       "title": "示例执行器",
   *       "createdTime": "2020-09-09 17:50:36",
   *       "updateTime": "2020-09-09 17:50:36"
   *     }
   *   }
   *
   *   <b>失败响应示例:</b>
   *   {
   *     "success": false,
   *     "code": 400,
   *     "message": "appName已存在"
   *   }
   * </pre>
   *
   * @param args 新建参数
   * @return 执行器ID
   * @author 宋志宗 on 2020/8/26 23:41
   */
  @Nonnull
  @Override
  @PostMapping("/create")
  public Mono<Res<JobWorkerRsp>> create(@Validated @RequestBody
                                        @Nonnull CreateWorkerArgs args) {
    return service.create(args).map(Res::data);
  }

  /**
   * 更新执行器信息
   * <pre>
   *   <b>请求示例:</b>
   *   POST http://{{host}}:{{port}}/{{gateway}}/worker/update
   *   Content-Type: application/json
   *
   *   {
   *     "workerId": 134930081742061568,
   *     "appName": "SAMPLE-EXECUTOR",
   *     "title": "示例执行器"
   *   }
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success",
   *     "data": {
   *       "workerId": "134917723116273664",
   *       "appName": "SAMPLE-EXECUTOR",
   *       "title": "示例执行器",
   *       "createdTime": "2020-09-09 17:50:36",
   *       "updateTime": "2020-09-09 17:50:36"
   *     }
   *   }
   *
   *   <b>失败响应示例:</b>
   *   {
   *     "success": false,
   *     "code": 400,
   *     "message": "appName已存在"
   *   }
   * </pre>
   *
   * @param args 更新参数
   * @return 更新结果
   * @author 宋志宗 on 2020/8/26 23:42
   */
  @Nonnull
  @Override
  @PostMapping("/update")
  public Mono<Res<JobWorkerRsp>> update(@Validated @RequestBody
                                        @Nonnull UpdateWorkerArgs args) {
    return service.update(args).map(Res::data);
  }

  /**
   * 删除执行器
   * <pre>
   *   <b>请求示例:</b>
   *   DELETE http://{{host}}:{{port}}/{{gateway}}/worker/delete/133799357949411328
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success"
   *   }
   * </pre>
   *
   * @param workerId 执行器ID
   * @return 删除结果
   * @author 宋志宗 on 2020/8/26 23:43
   */
  @Nonnull
  @Override
  @DeleteMapping("/delete/{workerId}")
  public Mono<Res<Void>> delete(@PathVariable("workerId") @Nonnull Long workerId) {
    return service.delete(workerId).map(b -> Res.success());
  }

  /**
   * 查询执行器列表
   * <pre>
   *   <b>请求示例:</b>
   *   GET http://{{host}}:{{port}}/{{gateway}}/worker/query?page=1&size=20&appName=SAMPLE
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success",
   *     "data": [
   *       {
   *         "workerId": "134930081742061568",
   *         "appName": "SAMPLE-EXECUTOR2",
   *         "title": "示例执行器22222",
   *         "createdTime": "2020-09-09 17:50:36",
   *         "updateTime": "2020-09-09 17:52:26"
   *       }
   *     ],
   *     "page": 1,
   *     "size": 20,
   *     "total": "1",
   *     "totalPages": 1
   *   }
   * </pre>
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 执行器信息列表
   * @author 宋志宗 on 2020/8/26 23:45
   */
  @Nonnull
  @Override
  @GetMapping("/query")
  public Mono<Res<List<JobWorkerRsp>>> query(@Nullable QueryWorkerArgs args,
                                             @Nullable Paging paging) {
    if (args == null) {
      args = new QueryWorkerArgs();
    }
    if (paging == null) {
      paging = Paging.of(1, 20);
    }
    paging.cleanOrders();
    paging.descBy("workerId");
    return service.query(args, paging);
  }
}
