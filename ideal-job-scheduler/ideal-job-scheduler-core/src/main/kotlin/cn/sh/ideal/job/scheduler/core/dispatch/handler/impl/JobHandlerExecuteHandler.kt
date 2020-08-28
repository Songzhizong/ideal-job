package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.executor.JobExecutor
import cn.sh.ideal.job.common.loadbalancer.LbFactory
import cn.sh.ideal.job.common.message.payload.ExecuteJobParam
import cn.sh.ideal.job.common.transfer.CommonResMsg
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService
import cn.sh.ideal.job.scheduler.core.admin.service.JobTriggerLogService
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerConfig
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandler
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Component("jobHandlerExecuteHandler")
final class JobHandlerExecuteHandler(
    private val lbFactory: LbFactory<JobExecutor>,
    private val jobExecutorService: JobExecutorService,
    private val triggerLogService: JobTriggerLogService,
    private val jobSchedulerConfig: JobSchedulerConfig) : ExecuteHandler {
  val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    ExecuteHandlerFactory.register(ExecuteTypeEnum.JOB_HANDLER, this)
  }

  override fun execute(jobInfo: JobInfo,
                       triggerType: TriggerTypeEnum,
                       customExecutorParam: String?): Res<Void> {
    var trigger = true
    val messageList = ArrayList<String>()
    val jobId = jobInfo.jobId
    val executorId = jobInfo.executorId
    val executor = jobExecutorService.loadById(executorId)

    val executorAppName = if (executor == null) {
      trigger = false
      messageList.add("执行器: $executorId 不存在")
      log.info("执行器: {} 不存在", executorId)
      ""
    } else {
      val appName = executor.appName
      if (StringUtils.isBlank(appName)) {
        trigger = false
        messageList.add("执行器应用名称为空")
        log.info("任务: {} 的执行器应用名称为空", jobId)
      }
      appName
    }

    if (StringUtils.isBlank(jobInfo.executorHandler)) {
      trigger = false
      messageList.add("执行处理器为空")
      log.info("任务: {} 的执行处理器为空", jobId)
    }

    var reachableServers: List<JobExecutor> = emptyList()
    var chooseServer: JobExecutor? = null
    if (StringUtils.isNotBlank(executorAppName)) {
      val serverHolder = lbFactory
          .getServerHolder(executorAppName)
      reachableServers = serverHolder.reachableServers
      if (reachableServers.isEmpty()) {
        trigger = false
        messageList.add("$executorAppName 没有可用的执行器实例")
        log.info("执行器: {} 当前没有可用的实例", executorAppName)
      } else {
        val loadBalancer = lbFactory
            .getLoadBalancer(executorAppName, jobInfo.routeStrategy.lbStrategy!!)
        chooseServer = loadBalancer
            .chooseServer(jobId, reachableServers)
        if (chooseServer == null) {
          trigger = false
          messageList.add("$executorAppName 选取的执行器实例为空")
          log.info("执行器: {} 选取实例为空", executorAppName)
        }
      }
    }

    val triggerLog = JobTriggerLog.createInitialized()
    triggerLog.executorId = executorId
    triggerLog.executeType = ExecuteTypeEnum.JOB_HANDLER
    triggerLog.jobId = jobInfo.jobId
    triggerLog.triggerType = triggerType
    triggerLog.schedulerInstance = jobSchedulerConfig.ipPort
    if (reachableServers.isNotEmpty()) {
      triggerLog.availableInstances = reachableServers
          .joinToString(",") { it.instanceId }
    }
    if (chooseServer != null) {
      triggerLog.executeInstances = chooseServer.instanceId
    }
    triggerLog.routeStrategy = jobInfo.routeStrategy
    triggerLog.blockStrategy = jobInfo.blockStrategy
    triggerLog.executorHandler = jobInfo.executorHandler
    val executorParam = customExecutorParam ?: jobInfo.executorParam
    triggerLog.executorParam = executorParam
    // triggerLog.executorShardingParam
    triggerLog.retryCount = jobInfo.retryCount
    val res = if (trigger) {
      triggerLog.triggerCode = JobTriggerLog.TRIGGER_CODE_SUCCESS
      triggerLog.triggerMsg = "success"
      Res.success<Void>()
    } else {
      triggerLog.triggerCode = JobTriggerLog.TRIGGER_CODE_FAIL
      val triggerMessage = "调度失败: " + messageList.joinToString(",")
      triggerLog.triggerMsg = triggerMessage
      Res.err(triggerMessage)
    }

    triggerLogService.saveLog(triggerLog)

    val jobParam = ExecuteJobParam()
    jobParam.jobId = jobId.toString()
    jobParam.triggerId = triggerLog.triggerId
    jobParam.executorHandler = jobInfo.executorHandler
    jobParam.executorParams = executorParam
    jobParam.blockStrategy = jobInfo.blockStrategy.code
    try {
      chooseServer?.executeJob(jobParam)
    } catch (e: Exception) {
      // 出现异常则调度失败, 需要对调度日志进行调整
      val errMsg = "${e.javaClass.name}:${e.message}"
      val triggerMessage = "远程服务调用异常: $errMsg"
      triggerLog.triggerCode = JobTriggerLog.TRIGGER_CODE_FAIL
      triggerLog.triggerMsg = triggerMessage
      log.info(triggerMessage)
      triggerLogService.saveLog(triggerLog)
      return Res.err(CommonResMsg.INTERNAL_SERVER_ERROR, errMsg)
    }
    return res
  }
}