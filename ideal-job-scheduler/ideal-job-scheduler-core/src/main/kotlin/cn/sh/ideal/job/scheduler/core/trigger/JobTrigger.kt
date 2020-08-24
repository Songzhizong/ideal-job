package cn.sh.ideal.job.scheduler.core.trigger

import cn.sh.ideal.job.common.executor.JobExecutor
import cn.sh.ideal.job.common.loadbalancer.LbFactory
import cn.sh.ideal.job.common.message.payload.ExecuteJobParam
import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import cn.sh.ideal.job.scheduler.core.admin.service.JobTriggerLogService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Component
class JobTrigger(private val lbFactory: LbFactory<JobExecutor>,
                 private val triggerLogService: JobTriggerLogService) {

  /**
   * 触发任务
   */
  fun trigger(triggerParam: TriggerParam) {
    var trigger = true
    val messageList = ArrayList<String>()

    val jobId = triggerParam.jobId
    val executorId = triggerParam.executorId
    val triggerType = triggerParam.triggerType
    val executorAppName = triggerParam.executorAppName
    if (StringUtils.isBlank(executorAppName)) {
      trigger = false
      messageList.add("执行器应用名称为空")
    }
    val routeStrategy = triggerParam.routeStrategy
    val executorHandler = triggerParam.executorHandler
    if (StringUtils.isBlank(executorHandler)) {
      trigger = false
      messageList.add("执行处理器为空")
    }
    val executorParam = triggerParam.executorParam
    val blockStrategy = triggerParam.blockStrategy
    val retryCount = triggerParam.retryCount

    val serverHolder = lbFactory.getServerHolder(executorAppName)
    val reachableServers = serverHolder.reachableServers
    val chooseServer = if (reachableServers.isEmpty()) {
      trigger = false
      messageList.add("没有可用的执行器实例")
      null
    } else {
      val loadBalancer = lbFactory.getLoadBalancer(executorAppName, routeStrategy)
      val chooseServer = loadBalancer.chooseServer(jobId, serverHolder)
      if (chooseServer == null) {
        trigger = false
        messageList.add("选取的执行器实例为空")
      }
      chooseServer
    }

    val log = JobTriggerLog.createInitialized()
    log.executorId = executorId
    log.jobId = jobId
    log.triggerType = triggerType
    // todo log.schedulerInstance
    if (reachableServers.isNotEmpty()) {
      log.availableInstances = reachableServers.joinToString(",") { it.instanceId }
    }
    if (chooseServer != null) {
      log.executeInstances = chooseServer.instanceId
    }
    log.routeStrategy = routeStrategy
    log.blockStrategy = blockStrategy
    log.executorHandler = executorHandler
    log.executorParam = executorParam
    // log.executorShardingParam
    log.retryCount = retryCount
    if (trigger) {
      log.triggerCode = JobTriggerLog.TRIGGER_CODE_SUCCESS
      log.triggerMsg = "success"
    } else if (messageList.isNotEmpty()) {
      log.triggerCode = JobTriggerLog.TRIGGER_CODE_FAIL
      log.triggerMsg = "调度失败: " + messageList.joinToString(",")
    }
    triggerLogService.saveLog(log)
    val triggerId = log.triggerId

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
      }
    }
  }
}