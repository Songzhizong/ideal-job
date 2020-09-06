package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import com.zzsong.job.scheduler.core.pojo.JobInfo;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.storage.JobInfoStorage;
import com.zzsong.job.scheduler.core.converter.JobInfoConverter;
import com.zzsong.job.scheduler.core.dispatch.JobDispatcher;
import com.zzsong.job.scheduler.core.utils.CronExpression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class JobService {
  private static final Logger log = LoggerFactory.getLogger(JobService.class);
  private static final long PRE_READ_MS = 5000L;

  @Autowired
  private JobDispatcher jobDispatcher;

  private final JobInfoStorage jobInfoStorage;
  private final JobWorkerService jobWorkerService;

  public JobService(JobInfoStorage jobInfoStorage,
                    JobWorkerService jobWorkerService) {
    this.jobInfoStorage = jobInfoStorage;
    this.jobWorkerService = jobWorkerService;
  }

  /**
   * 新建任务
   *
   * @param createJobArgs 新增任务请求参数
   * @return 任务id
   * @author 宋志宗
   * @date 2020/8/26 7:36 下午
   */
  public Mono<JobInfoRsp> createJob(@Nonnull CreateJobArgs createJobArgs) {
    long workerId = createJobArgs.getWorkerId();
    boolean autoStart = createJobArgs.isAutoStart();
    String cron = createJobArgs.getCron();
    return jobWorkerService.loadById(workerId)
        .flatMap(workerOptional -> {
          if (!workerOptional.isPresent()) {
            log.info("新建任务失败, 执行器: {} 不存在", workerId);
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND, "执行器不存在"));
          }
          LocalDateTime now = DateTimes.now();
          JobInfo jobInfo = JobInfoConverter.fromCreateJobArgs(createJobArgs);
          jobInfo.setCreatedTime(now);
          jobInfo.setUpdateTime(now);
          if (StringUtils.isNotBlank(cron)) {
            boolean validExpression = CronExpression.isValidExpression(cron);
            if (!validExpression) {
              log.info("cron表达式: {} 不合法", cron);
              return Mono.error(new VisibleException(CommonResMsg.BAD_REQUEST, "cron表达式不合法"));
            }
            if (autoStart) {
              long nextTriggerTime = getNextTriggerTime(cron);
              jobInfo.setJobStatus(JobInfo.JOB_START);
              jobInfo.setNextTriggerTime(nextTriggerTime);
            }
          }
          return jobInfoStorage.save(jobInfo).map(JobInfoConverter::toJobInfoRsp);
        });
  }

  /**
   * 更新任务信息
   *
   * @param args 更新参数
   * @author 宋志宗
   * @date 2020/8/26 8:48 下午
   */
  public Mono<JobInfoRsp> updateJob(@Nonnull UpdateJobArgs args) {
    long jobId = args.getJobId();
    String cron = args.getCron();
    return jobInfoStorage.findById(jobId)
        .flatMap(jobInfoOptional -> {
          if (!jobInfoOptional.isPresent()) {
            log.info("任务: {} 不存在", jobId);
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在"));
          }
          LocalDateTime now = DateTimes.now();
          JobInfo jobInfo = jobInfoOptional.get();
          BeanUtils.copyProperties(args, jobInfo);
          jobInfo.setUpdateTime(now);
          if (StringUtils.isNotBlank(cron)) {
            boolean validExpression = CronExpression.isValidExpression(cron);
            if (!validExpression) {
              log.info("cron表达式: {} 不合法", cron);
              return Mono.error(new VisibleException(CommonResMsg.BAD_REQUEST, "cron表达式不合法"));
            }
            if (jobInfo.getJobStatus() == JobInfo.JOB_START) {
              long nextTriggerTime = getNextTriggerTime(cron);
              jobInfo.setNextTriggerTime(nextTriggerTime);
            }
          } else {
            jobInfo.setJobStatus(JobInfo.JOB_STOP);
          }
          return jobInfoStorage.save(jobInfo).map(JobInfoConverter::toJobInfoRsp);
        });
  }

  /**
   * 移除任务
   *
   * @param jobId 任务id
   * @author 宋志宗
   * @date 2020/8/26 8:49 下午
   */
  public Mono<Integer> removeJob(long jobId) {
    return jobInfoStorage.findById(jobId)
        .flatMap(jobInfoOptional -> {
          if (!jobInfoOptional.isPresent()) {
            log.info("任务: {} 不存在", jobId);
            return Mono.just(0);
          }
          return jobInfoStorage.delete(jobId);
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
  public Mono<Res<List<JobInfoRsp>>> query(@Nonnull QueryJobArgs args, @Nonnull Paging paging) {
    return jobInfoStorage.query(args, paging)
        .map(listRes ->
            listRes.convertNewRes(list ->
                list.stream()
                    .map(JobInfoConverter::toJobInfoRsp)
                    .collect(Collectors.toList())
            )
        );
  }

  /**
   * 启用任务
   *
   * @param jobId 任务id
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  public Mono<Boolean> enableJob(long jobId) {
    return jobInfoStorage.findById(jobId)
        .flatMap(jobInfoOptional -> {
          if (!jobInfoOptional.isPresent()) {
            log.info("任务: {} 不存在", jobId);
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在"));
          }
          JobInfo jobInfo = jobInfoOptional.get();
          if (jobInfo.getJobStatus() == JobInfo.JOB_START) {
            log.info("任务: {} 正在在运行中", jobId);
            return Mono.just(true);
          }
          String cron = jobInfo.getCron();
          if (StringUtils.isBlank(cron)) {
            log.info("启动任务: {} 失败, cron表达式为空", jobId);
            return Mono.error(new VisibleException("cron表达式为空"));
          }
          long nextTriggerTime = getNextTriggerTime(cron);
          jobInfo.setJobStatus(JobInfo.JOB_START);
          jobInfo.setLastTriggerTime(0);
          jobInfo.setNextTriggerTime(nextTriggerTime);
          return jobInfoStorage.save(jobInfo).map(j -> true);
        });
  }

  /**
   * 停用任务
   *
   * @param jobId 任务id
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  public Mono<Boolean> disableJob(long jobId) {
    return jobInfoStorage.findById(jobId)
        .flatMap(jobInfoOptional -> {
          if (!jobInfoOptional.isPresent()) {
            log.info("任务: {} 不存在", jobId);
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在"));
          }
          JobInfo jobInfo = jobInfoOptional.get();
          if (jobInfo.getJobStatus() == JobInfo.JOB_STOP) {
            log.info("任务: {} 为停止状态", jobId);
            return Mono.just(true);
          }
          jobInfo.setJobStatus(JobInfo.JOB_STOP);
          jobInfo.setLastTriggerTime(0);
          jobInfo.setNextTriggerTime(0);
          return jobInfoStorage.save(jobInfo).map(j -> true);
        });
  }

  public Mono<Boolean> triggerJob(long jobId, @Nullable String customExecuteParam) {
    return jobInfoStorage.findJobViewById(jobId)
        .flatMap(jobViewOptional -> {
          if (!jobViewOptional.isPresent()) {
            log.info("任务: {} 不存在", jobId);
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在"));
          }
          JobView jobView = jobViewOptional.get();
          return jobDispatcher.dispatch(jobView, TriggerTypeEnum.MANUAL, customExecuteParam);
        });
  }

  /**
   * 读取一批待执行的任务
   *
   * @param maxNextTime 最大下次执行时间
   * @param count       读取数量
   * @return 待执行任务列表
   * @date 2020/8/24 8:46 下午
   */
  public Flux<JobView> loadScheduleJobViews(long maxNextTime, int count) {
    Paging paging = Paging.of(1, count).ascBy("jobId");
    return jobInfoStorage.loadScheduleJobViews(maxNextTime, paging);
  }

  public Mono<Integer> batchUpdateTriggerInfo(List<JobView> viewList) {
    return jobInfoStorage.batchUpdateTriggerInfo(viewList);
  }


  private long getNextTriggerTime(String cron) {
    long benchmark = System.currentTimeMillis() + PRE_READ_MS;
    Date nextValidTime;
    try {
      nextValidTime = new CronExpression(cron)
          .getNextValidTimeAfter(new Date(benchmark));
    } catch (ParseException e) {
      String errMsg = e.getClass().getName() + ": " + e.getMessage();
      log.info("解析cron: {} 异常: {}", cron, errMsg);
      throw new VisibleException("解析cron表达式出现异常");
    }
    if (nextValidTime == null) {
      log.info("尝试通过cron: {} 获取下次执行时间失败", cron);
      throw new VisibleException("获取下次执行时间失败");
    }
    return nextValidTime.getTime();
  }
}
