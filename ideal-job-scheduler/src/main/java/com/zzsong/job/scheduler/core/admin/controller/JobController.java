package com.zzsong.job.scheduler.core.admin.controller;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.common.utils.DateUtils;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import com.zzsong.job.scheduler.core.admin.service.JobService;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.JobClient;
import com.zzsong.job.scheduler.core.utils.CronExpression;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Job管理
 *
 * @author 宋志宗 on 2020/8/20
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
   * 新建Job
   * <pre>
   *   <b>请求示例:</b>
   *   POST http://{{host}}:{{port}}/{{gateway}}/job/create
   *   Content-Type: application/json
   *
   *   {
   *     "autoStart": false,
   *     "executorId": 134511511480565760,
   *     "executeType": "JOB_HANDLER",
   *     "executorHandler": "demoJobHandler",
   *     "executeParam": "test",
   *     "routeStrategy": "CONSISTENT_HASH",
   *     "blockStrategy": "SERIAL",
   *     "cron": "0/30 * * * * ?",
   *     "retryCount": 0,
   *     "jobName": "test",
   *     "alarmEmail": "",
   *     "application": "",
   *     "tenantId": "",
   *     "bizType": "",
   *     "customTag": "",
   *     "businessId": ""
   *   }
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success",
   *     "data": {
   *       "jobId": "134938329517916160",
   *       "application": "",
   *       "tenantId": "",
   *       "bizType": "",
   *       "customTag": "",
   *       "businessId": "",
   *       "executorId": "134930081742061568",
   *       "cron": "0/30 * * * * ?",
   *       "jobName": "test",
   *       "alarmEmail": "",
   *       "routeStrategy": "CONSISTENT_HASH",
   *       "executorHandler": "demoJobHandler",
   *       "executeParam": "test",
   *       "blockStrategy": "SERIAL",
   *       "retryCount": 0,
   *       "jobStatus": 0,
   *       "createdTime": "2020-09-09 18:23:22",
   *       "updateTime": "2020-09-09 18:23:22"
   *     }
   *   }
   *
   *   <b>失败响应示例:</b>
   *   {
   *     "success": false,
   *     "code": 400,
   *     "message": "执行器不存在"
   *   }
   * </pre>
   *
   * @param createJobArgs 新增任务请求参数
   * @return 任务id
   * @author 宋志宗 on 2020/8/26 7:36 下午
   */
  @Nonnull
  @Override
  @PostMapping("/create")
  public Mono<Res<JobInfoRsp>> create(@Validated @RequestBody
                                      @Nonnull CreateJobArgs createJobArgs) {
    return jobService.createJob(createJobArgs).map(Res::data);
  }

  /**
   * 更新Job信息
   * <pre>
   *   <b>请求示例:</b>
   *   POST http://{{host}}:{{port}}/{{gateway}}/job/update
   *   Content-Type: application/json
   *
   *   {
   *     "jobId": 134938329517916160,
   *     "executorId": 133924649984589824,
   *     "executorHandler": "",
   *     "executeParam": "",
   *     "routeStrategy": "WEIGHT_ROUND_ROBIN",
   *     "blockStrategy": "PARALLEL",
   *     "cron": "0 0/5 * * * ?",
   *     "retryCount": 0,
   *     "jobName": "",
   *     "alarmEmail": ""
   *   }
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success",
   *     "data": {
   *       "jobId": "134938329517916160",
   *       "application": "",
   *       "tenantId": "",
   *       "bizType": "",
   *       "customTag": "",
   *       "businessId": "",
   *       "executorId": "133924649984589824",
   *       "cron": "0 0/5 * * * ?",
   *       "jobName": "",
   *       "alarmEmail": "",
   *       "routeStrategy": "WEIGHT_ROUND_ROBIN",
   *       "executorHandler": "",
   *       "executeParam": "",
   *       "blockStrategy": "PARALLEL",
   *       "retryCount": 0,
   *       "jobStatus": 0,
   *       "createdTime": "2020-09-09 18:23:23",
   *       "updateTime": "2020-09-09 18:24:55"
   *     }
   *   }
   *
   *   <b>失败响应示例:</b>
   *   {
   *     "success": false,
   *     "code": 400,
   *     "message": "执行器不存在"
   *   }
   * </pre>
   *
   * @param updateJobArgs 更新参数
   * @return 更新结果
   * @author 宋志宗 on 2020/8/26 8:48 下午
   */
  @Nonnull
  @Override
  @PostMapping("/update")
  public Mono<Res<JobInfoRsp>> update(@Validated @RequestBody
                                      @Nonnull UpdateJobArgs updateJobArgs) {
    return jobService.updateJob(updateJobArgs).map(Res::data);
  }

  /**
   * 移除Job
   * <pre>
   *   <b>请求示例:</b>
   *   DELETE http://{{host}}:{{port}}/{{gateway}}/job/remove/134938329517916160
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success"
   *   }
   * </pre>
   *
   * @param jobId 任务id
   * @return 移除结果
   * @author 宋志宗 on 2020/8/26 8:49 下午
   */
  @Nonnull
  @Override
  @DeleteMapping("/remove/{jobId}")
  public Mono<Res<Void>> remove(@PathVariable("jobId") @Nonnull Long jobId) {
    return jobService.removeJob(jobId).map(b -> Res.success());
  }

  /**
   * 查询Job列表
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 任务信息列表
   * @author 宋志宗 on 2020/8/26 8:51 下午
   */
  @Nonnull
  @Override
  @PostMapping("/query")
  public Mono<Res<List<JobInfoRsp>>> query(@RequestBody @Nonnull QueryJobArgs args,
                                           @Nullable Paging paging) {
    if (paging == null) {
      paging = Paging.of(1, 20);
    }
    paging.cleanOrders().descBy("jobId");
    return jobService.query(args, paging);
  }

  /**
   * 启用Job
   * <pre>
   *   <b>请求示例:</b>
   *   PUT http://{{host}}:{{port}}/{{gateway}}/job/enable/134938329517916160
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success"
   *   }
   * </pre>
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗 on 2020/8/20 4:38 下午
   */
  @Nonnull
  @Override
  @PutMapping("/enable/{jobId}")
  public Mono<Res<Void>> enable(@PathVariable("jobId") @Nonnull Long jobId) {
    return jobService.enableJob(jobId).map(b -> Res.success());
  }

  /**
   * 停用Job
   * <pre>
   *   <b>请求示例:</b>
   *   PUT http://{{host}}:{{port}}/{{gateway}}/job/disable/134938329517916160
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success"
   *   }
   * </pre>
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗 on 2020/8/20 4:38 下午
   */
  @Nonnull
  @Override
  @PutMapping("/disable/{jobId}")
  public Mono<Res<Void>> disable(@PathVariable("jobId") @Nonnull Long jobId) {
    return jobService.disableJob(jobId).map(b -> Res.success());
  }

  /**
   * 触发Job
   * <pre>
   *   <b>请求body可以是json/字符串/数字</b>
   *   <b>请求示例:</b>
   *   POST http://{{host}}:{{port}}/{{gateway}}/job/trigger/134938329517916160
   *   Content-Type: application/json
   *
   *   hello_world
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success"
   *   }
   *   <b>失败响应示例:</b>
   *   {
   *     "success": false,
   *     "code": 400,
   *     "message": "任务: 134938329517916160 客户端: 127.0.0.1:9904 线程池资源不足"
   *   }
   * </pre>
   *
   * @param jobId        任务id
   * @param executeParam 执行参数, 为<code>null</code>空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗 on 2020/8/20 4:18 下午
   */
  @Nonnull
  @Override
  @PostMapping("/trigger/{jobId}")
  public Mono<Res<Void>> trigger(@PathVariable("jobId") @Nonnull Long jobId,
                                 @RequestBody(required = false)
                                 @Nullable String executeParam) {
    return jobService.triggerJob(jobId, executeParam);
  }

  /**
   * 获取执行计划
   * <p>通过cron表达式获取接下来几次的执行时间</p>
   * <pre>
   *   <b>请求示例:</b>
   *   GET http://{{host}}:{{port}}/{{gateway}}/job/triggerPlan?count=5&cron=0%200%2012%20*%20*%20?%20*
   *
   *   <b>成功响应示例:</b>
   *   {
   *     "success": true,
   *     "code": 200,
   *     "message": "Success",
   *     "data": [
   *       "2020-09-12 12:00:00",
   *       "2020-09-13 12:00:00",
   *       "2020-09-14 12:00:00",
   *       "2020-09-15 12:00:00",
   *       "2020-09-16 12:00:00"
   *     ]
   *   }
   * </pre>
   *
   * @param count 次数
   * @param cron  cron表达式, UrlEncode
   * @return 执行计划
   */
  @GetMapping("/triggerPlan")
  public Mono<Res<List<String>>> triggerPlan(@RequestParam(defaultValue = "5")
                                             @Min(value = 1, message = "count至少为1")
                                             @Max(value = 10, message = "count最大为10") int count,
                                             @NotBlank(message = "cron不能为空") @Nonnull String cron) {

    CronExpression expression;
    try {
      expression = new CronExpression(cron);
    } catch (ParseException e) {
      throw new VisibleException("cron表达式不合法");
    }
    List<String> plans = new ArrayList<>();
    Date lastTime = new Date();
    for (int i = 0; i < count; i++) {
      lastTime = expression.getNextValidTimeAfter(lastTime);
      if (lastTime != null) {
        plans.add(DateUtils.format(lastTime, DateTimes.yyyy_MM_dd_HH_mm_ss));
      } else {
        break;
      }
    }
    return Mono.just(Res.data(plans));
  }
}
