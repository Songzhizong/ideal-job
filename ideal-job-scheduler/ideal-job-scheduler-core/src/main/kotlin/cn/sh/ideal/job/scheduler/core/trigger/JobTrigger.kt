package cn.sh.ideal.job.scheduler.core.trigger

import cn.sh.ideal.job.common.constants.HandleStatusEnum
import cn.sh.ideal.job.common.executor.JobExecutor
import cn.sh.ideal.job.common.loadbalancer.LbFactory
import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback
import cn.sh.ideal.job.common.message.payload.ExecuteJobParam
import cn.sh.ideal.job.common.transfer.CommonResMsg
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService
import cn.sh.ideal.job.scheduler.core.admin.service.JobTriggerLogService
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Component
class JobTrigger(private val lbFactory: LbFactory<JobExecutor>,
                 private val jobExecutorService: JobExecutorService,
                 private val triggerLogService: JobTriggerLogService) {
  val log: Logger = LoggerFactory.getLogger(this.javaClass)

  /**
   * 触发任务
   */
  fun trigger(triggerParam: TriggerParam): Res<Void> {
    var trigger = true
    val messageList = ArrayList<String>()
    val jobId = triggerParam.jobId
    val executorId = triggerParam.executorId
    val executor = jobExecutorService.loadById(executorId)
    val executorAppName = if (executor == null) {
      trigger = false
      messageList.add("执行器: $executorId 不存在")
      log.info("执行器: {} 不存在", executorId)
      ""
    } else {
      executor.appName
    }
    if (StringUtils.isBlank(executorAppName)) {
      trigger = false
      messageList.add("执行器应用名称为空")
      log.info("任务: {} 的执行器应用名称为空", jobId)
    }
    val triggerType = triggerParam.triggerType
    val routeStrategy = triggerParam.routeStrategy
    val executorHandler = triggerParam.executorHandler
    if (StringUtils.isBlank(executorHandler)) {
      trigger = false
      messageList.add("执行处理器为空")
      log.info("任务: {} 的执行处理器为空", jobId)
    }
    val executorParam = triggerParam.executorParam
    val blockStrategy = triggerParam.blockStrategy
    val retryCount = triggerParam.retryCount

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
            .getLoadBalancer(executorAppName, routeStrategy)
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
    triggerLog.jobId = jobId
    triggerLog.triggerType = triggerType
    // todo log.schedulerInstance
    if (reachableServers.isNotEmpty()) {
      triggerLog.availableInstances = reachableServers
          .joinToString(",") { it.instanceId }
    }
    if (chooseServer != null) {
      triggerLog.executeInstances = chooseServer.instanceId
    }
    triggerLog.routeStrategy = routeStrategy
    triggerLog.blockStrategy = blockStrategy
    triggerLog.executorHandler = executorHandler
    triggerLog.executorParam = executorParam
    // log.executorShardingParam
    triggerLog.retryCount = retryCount
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
    val triggerId = triggerLog.triggerId

    if (trigger) {
      val jobParam = ExecuteJobParam()
      jobParam.jobId = jobId.toString()
      jobParam.triggerId = triggerId
      jobParam.executorHandler = executorHandler
      jobParam.executorParams = executorParam
      jobParam.blockStrategy = blockStrategy.code
      try {
        chooseServer!!.executeJob(jobParam)
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
    }
    return res
  }

  fun triggerCallback(executeJobCallback: ExecuteJobCallback) {
    val sequence = executeJobCallback.sequence
//    val jobId = executeJobCallback.jobId
    val triggerId = executeJobCallback.triggerId
    val handleStatus = executeJobCallback.handleStatus
    val handleMessage = executeJobCallback.handleMessage
    val handleTime = executeJobCallback.handleTime

    val instant = Instant.ofEpochMilli(handleTime)
    // cn.sh.ideal.job.scheduler.core.admin.repository.JobTriggerLogRepository.updateWhenTriggerCallback
    // triggerLog 设置参数务必和上述方法中的参数一致
    // 收到执行回调直接更新日志中变化的部分即可
    val triggerLog = JobTriggerLog()
    triggerLog.triggerId = triggerId
    triggerLog.handleTime = LocalDateTime.ofInstant(instant, ZoneOffset.of("+8"))
    triggerLog.handleStatus = HandleStatusEnum.valueOfCode(handleStatus)
    triggerLog.handleMsg = handleMessage
    triggerLog.handleSequence = sequence
    triggerLog.updateTime = LocalDateTime.now()
    triggerLogService.updateWhenTriggerCallback(triggerLog)
  }
}