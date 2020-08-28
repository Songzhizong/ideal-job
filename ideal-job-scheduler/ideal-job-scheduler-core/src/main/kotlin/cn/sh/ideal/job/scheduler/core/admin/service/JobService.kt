package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.common.constants.BlockStrategyEnum
import cn.sh.ideal.job.common.constants.DBDefaults
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum
import cn.sh.ideal.job.common.transfer.CommonResMsg
import cn.sh.ideal.job.common.transfer.Paging
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.common.transfer.SpringPages
import cn.sh.ideal.job.scheduler.api.dto.req.CreateJobArgs
import cn.sh.ideal.job.scheduler.api.dto.req.QueryJobArgs
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateJobArgs
import cn.sh.ideal.job.scheduler.api.dto.rsp.JobInfoRsp
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInfoRepository
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInfoRepositoryCustom
import cn.sh.ideal.job.scheduler.core.converter.JobInfoConverter
import cn.sh.ideal.job.scheduler.core.trigger.JobTrigger
import cn.sh.ideal.job.scheduler.core.trigger.TriggerParam
import cn.sh.ideal.job.scheduler.core.utils.CronExpression
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.criteria.Predicate

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Suppress("DuplicatedCode")
@Service
class JobService(private val jobInfoRepository: JobInfoRepository) {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)
  private val preReadMs = 5000L

  @Autowired
  private lateinit var jobTrigger: JobTrigger

  @Autowired
  private lateinit var jobExecutorService: JobExecutorService

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
   * 更新任务信息
   *
   * @param updateJobArgs 更新参数
   * @return 更新结果
   * @author 宋志宗
   * @date 2020/8/26 8:48 下午
   */
  fun updateJob(updateJobArgs: UpdateJobArgs): Res<Void> {
    val jobId = updateJobArgs.jobId
    val jobInfo = jobInfoRepository.findByIdOrNull(jobId)
    if (jobInfo == null) {
      log.info("任务: {} 不存在", jobId)
      return Res.err(CommonResMsg.NOT_FOUND, "任务不存在")
    }
    val executorId = updateJobArgs.executorId
    val executorHandler = updateJobArgs.executorHandler ?: DBDefaults.DEFAULT_STRING_VALUE
    val executorParam = updateJobArgs.executorParam ?: DBDefaults.DEFAULT_STRING_VALUE
    val routeStrategy = updateJobArgs.routeStrategy ?: LbStrategyEnum.POLLING
    val blockStrategy = updateJobArgs.blockStrategy ?: BlockStrategyEnum.SERIAL
    val cron = updateJobArgs.cron ?: DBDefaults.DEFAULT_STRING_VALUE
    val retryCount = updateJobArgs.retryCount ?: DBDefaults.DEFAULT_INT_VALUE
    val jobName = updateJobArgs.jobName ?: DBDefaults.DEFAULT_STRING_VALUE
    val alarmEmail = updateJobArgs.alarmEmail ?: DBDefaults.DEFAULT_STRING_VALUE

    jobInfo.executorId = executorId
    jobInfo.executorHandler = executorHandler
    jobInfo.executorParam = executorParam
    jobInfo.routeStrategy = routeStrategy
    jobInfo.blockStrategy = blockStrategy
    jobInfo.retryCount = retryCount
    jobInfo.jobName = jobName
    jobInfo.alarmEmail = alarmEmail
    if (cron.isNotBlank()) {
      val validExpression = CronExpression.isValidExpression(cron)
      if (!validExpression) {
        return Res.err(CommonResMsg.BAD_REQUEST, "cron表达式不合法")
      }
      jobInfo.cron = cron
      if (jobInfo.jobStatus == JobInfo.JOB_START) {
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
        val nextTriggerTime = nextValidTime.time
        jobInfo.nextTriggerTime = nextTriggerTime
      }
    }
    jobInfoRepository.save(jobInfo)
    return Res.success()
  }

  /**
   * 移除任务
   *
   * @param jobId 任务id
   * @return 移除结果
   * @author 宋志宗
   * @date 2020/8/26 8:49 下午
   */
  fun removeJob(jobId: Long): Res<Void> {
    val jobInfo = jobInfoRepository.findByIdOrNull(jobId)
    if (jobInfo == null) {
      log.info("任务: {} 不存在", jobId)
      return Res.success(CommonResMsg.NOT_FOUND)
    }
    jobInfoRepository.delete(jobInfo)
    return Res.success()
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
  fun query(args: QueryJobArgs, paging: Paging): Res<List<JobInfoRsp>> {
    val executorId = args.executorId
    val jobName = args.jobName
    val executorHandler = args.executorHandler
    val jobStatus = args.jobStatus
    val application = args.application
    val tenantId = args.tenantId
    val bizType = args.bizType
    val customTag = args.customTag
    val businessId = args.businessId

    val page = jobInfoRepository.findAll(
        { root, cq, cb ->
          val predicates = ArrayList<Predicate>()
          if (executorId != null && executorId > 0) {
            predicates.add(cb.equal(root.get<Any>("executorId"), executorId))
          }
          if (StringUtils.isNotBlank(jobName)) {
            predicates.add(cb.equal(root.get<Any>("jobName"), jobName))
          }
          if (StringUtils.isNotBlank(executorHandler)) {
            predicates.add(cb.equal(root.get<Any>("executorHandler"), executorHandler))
          }
          if (jobStatus != null && jobStatus == JobInfo.JOB_START) {
            predicates.add(cb.equal(root.get<Any>("jobStatus"), JobInfo.JOB_START))
          } else if (jobStatus != null && jobStatus == JobInfo.JOB_STOP) {
            predicates.add(cb.equal(root.get<Any>("jobStatus"), JobInfo.JOB_STOP))
          }
          if (StringUtils.isNotBlank(application)) {
            predicates.add(cb.equal(root.get<Any>("application"), application))
          }
          if (StringUtils.isNotBlank(tenantId)) {
            predicates.add(cb.equal(root.get<Any>("tenantId"), tenantId))
          }
          if (StringUtils.isNotBlank(bizType)) {
            predicates.add(cb.equal(root.get<Any>("bizType"), bizType))
          }
          if (StringUtils.isNotBlank(customTag)) {
            predicates.add(cb.equal(root.get<Any>("customTag"), customTag))
          }
          if (StringUtils.isNotBlank(businessId)) {
            predicates.add(cb.equal(root.get<Any>("businessId"), businessId))
          }
          cq.where(*predicates.toTypedArray()).restriction
        }, SpringPages.paging2Pageable(paging)
    )
    return SpringPages.toPageRes(page) { JobInfoConverter.toJobInfoRsp(it) }
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
    val triggerParam = TriggerParam()
    triggerParam.jobId = jobId
    triggerParam.executorId = executorId
    triggerParam.triggerType = TriggerTypeEnum.MANUAL
    triggerParam.executorHandler = jobInfo.executorHandler
    triggerParam.executorParam = executorParam ?: jobInfo.executorParam
    triggerParam.routeStrategy = jobInfo.routeStrategy
    triggerParam.blockStrategy = jobInfo.blockStrategy
    triggerParam.retryCount = jobInfo.retryCount
    return jobTrigger.trigger(triggerParam)
  }

  fun existsByExecutorId(executorId: Long): Boolean {
    return jobInfoRepository.existsByExecutorId(executorId)
  }

  fun loadScheduleJobs(maxNextTime: Long, count: Int): List<JobInfo> {
    val sort = Sort.by("jobId").ascending()
    val pageRequest = PageRequest.of(0, count, sort)
    val jobStart = JobInfo.JOB_START
    return jobInfoRepository
        .findAllByJobStatusAndNextTriggerTimeLessThanEqual(jobStart, maxNextTime, pageRequest)
  }

  fun batchUpdateTriggerInfo(jobInfoList: List<JobInfo>) {
    jobInfoRepository.batchUpdateTriggerInfo(jobInfoList)
  }
}