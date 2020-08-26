package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.transfer.CommonResMsg
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.scheduler.api.dto.req.CreateJobArgs
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInfoRepository
import cn.sh.ideal.job.scheduler.core.converter.JobInfoConverter
import cn.sh.ideal.job.scheduler.core.trigger.JobTrigger
import cn.sh.ideal.job.scheduler.core.trigger.TriggerParam
import cn.sh.ideal.job.scheduler.core.utils.CronExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Suppress("DuplicatedCode")
@Service
class JobService(private val jobTrigger: JobTrigger,
                 private val jobExecutorService: JobExecutorService,
                 private val jobInfoRepository: JobInfoRepository) {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)
  private val preReadMs = 5000L

  /**
   * 新建任务
   *
   * @param createJobArgs 新增任务请求参数
   * @return 任务id
   * @author 宋志宗
   * @date 2020/8/26 7:36 下午
   */
  fun createJob(createJobArgs: CreateJobArgs): Res<Long> {
    val executorId = createJobArgs.executorId
    val autoStart = createJobArgs.autoStart ?: false
    val cron = createJobArgs.cron ?: ""
    val executor = jobExecutorService.loadById(executorId)
    if (executor == null) {
      log.info("新建任务失败, 执行器: {} 不存在", executorId)
      return Res.err(CommonResMsg.NOT_FOUND, "执行器不存在")
    }
    val jobInfo = JobInfoConverter.fromCreateJobArgs(createJobArgs)
    if (autoStart && cron.isNotBlank()) {
      val validExpression = CronExpression.isValidExpression(cron)
      if (!validExpression) {
        return Res.err(CommonResMsg.BAD_REQUEST, "cron表达式不合法")
      }
      val benchmark = System.currentTimeMillis() + preReadMs
      val nextValidTime = try {
        CronExpression(cron).getNextValidTimeAfter(Date(benchmark))
      } catch (exception: Exception) {
        val errMessage = "${exception.javaClass.name}:${exception.message}"
        log.info("解析cron: {} 异常: {}", cron, errMessage)
        return Res.err(CommonResMsg.INTERNAL_SERVER_ERROR, "解析cron表达式出现异常")
      }
      if (nextValidTime == null) {
        log.info("尝试通过cron: {} 获取下次执行时间失败", cron)
        return Res.err(CommonResMsg.BAD_REQUEST, "获取下次执行时间失败")
      }
      jobInfo.jobStatus = JobInfo.JOB_START
      val nextTriggerTime = nextValidTime.time
      jobInfo.nextTriggerTime = nextTriggerTime
    }
    jobInfoRepository.save(jobInfo)
    val jobId = jobInfo.jobId
    return Res.data(jobId)
  }

  /**
   * 启动任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  fun startJob(jobId: Long): Res<Void> {
    val jobInfo = jobInfoRepository.findByIdOrNull(jobId)
    if (jobInfo == null) {
      log.info("任务: {} 不存在", jobId)
      return Res.err(CommonResMsg.NOT_FOUND, "任务不存在")
    }
    if (jobInfo.jobStatus == JobInfo.JOB_START) {
      log.info("任务: {} 正在在运行中", jobId)
      return Res.success()
    }
    val cron = jobInfo.cron
    if (cron.isBlank()) {
      log.info("启动任务: {} 失败, cron表达式为空", jobId)
      return Res.err(CommonResMsg.BAD_REQUEST, "cron表达式不合法")
    }
    val benchmark = System.currentTimeMillis() + preReadMs
    val nextValidTime = try {
      CronExpression(cron).getNextValidTimeAfter(Date(benchmark))
    } catch (exception: Exception) {
      val errMessage = "${exception.javaClass.name}:${exception.message}"
      log.info("解析cron: {} 异常: {}", cron, errMessage)
      return Res.err(CommonResMsg.INTERNAL_SERVER_ERROR, "解析cron表达式出现异常")
    }
    if (nextValidTime == null) {
      log.info("尝试通过cron: {} 获取下次执行时间失败", cron)
      return Res.err(CommonResMsg.BAD_REQUEST, "获取下次执行时间失败")
    }
    jobInfo.jobStatus = JobInfo.JOB_START
    jobInfo.lastTriggerTime = 0
    val nextTriggerTime = nextValidTime.time
    jobInfo.nextTriggerTime = nextTriggerTime
    jobInfoRepository.save(jobInfo)
    return Res.success()
  }

  /**
   * 停止任务
   *
   * @param jobId 任务id
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 4:38 下午
   */
  fun stopJob(jobId: Long): Res<Void> {
    val jobInfo = jobInfoRepository.findByIdOrNull(jobId)
    if (jobInfo == null) {
      log.info("任务: {} 不存在", jobId)
      return Res.err(CommonResMsg.NOT_FOUND, "任务不存在")
    }
    if (jobInfo.jobStatus == JobInfo.JOB_STOP) {
      log.info("任务: {} 为停止状态", jobId)
      return Res.success()
    }
    jobInfo.jobStatus = JobInfo.JOB_STOP
    jobInfo.lastTriggerTime = 0
    jobInfo.nextTriggerTime = 0
    jobInfoRepository.save(jobInfo)
    return Res.success()
  }

  /**
   * 触发任务
   *
   * @param jobId         任务id
   * @param executorParam 执行参数, 如果为<code>null</code则使用默认任务配置
   * @author 宋志宗
   * @date 2020/8/24 8:46 下午
   */
  fun trigger(jobId: Long, executorParam: String?): Res<Void> {
    val jobInfo = jobInfoRepository.findByIdOrNull(jobId)
    if (jobInfo == null) {
      log.info("任务: {} 不存在", jobId)
      return Res.err(CommonResMsg.NOT_FOUND, "任务不存在")
    }
    val executorId = jobInfo.executorId
    val executor = jobExecutorService.loadById(executorId)
    if (executor == null) {
      log.error("任务: {} 所对应的调度器信息不存在", jobId)
      return Res.err(CommonResMsg.NOT_FOUND, "调度器信息不存在")
    }
    val triggerParam = TriggerParam()
    triggerParam.jobId = jobId
    triggerParam.executorId = executorId
    triggerParam.triggerType = TriggerTypeEnum.MANUAL
    triggerParam.executorAppName = executor.appName
    triggerParam.executorHandler = jobInfo.executorHandler
    triggerParam.executorParam = executorParam ?: jobInfo.executorParam
    triggerParam.routeStrategy = jobInfo.routeStrategy
    triggerParam.blockStrategy = jobInfo.blockStrategy
    triggerParam.retryCount = jobInfo.retryCount
    return jobTrigger.trigger(triggerParam)
  }
}