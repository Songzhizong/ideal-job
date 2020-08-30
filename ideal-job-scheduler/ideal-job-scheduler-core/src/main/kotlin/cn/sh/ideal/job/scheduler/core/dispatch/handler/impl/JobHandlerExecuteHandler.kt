package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.ExecuteTypeEnum
import cn.sh.ideal.job.common.constants.RouteStrategyEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.exception.VisibleException
import cn.sh.ideal.job.common.executor.TaskExecutor
import cn.sh.ideal.job.common.loadbalancer.LbFactory
import cn.sh.ideal.job.common.message.payload.TaskParam
import cn.sh.ideal.job.common.transfer.CommonResMsg
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance
import cn.sh.ideal.job.scheduler.core.admin.service.JobExecutorService
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService
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
    private val lbFactory: LbFactory<TaskExecutor>,
    private val instanceService: JobInstanceService,
    private val jobExecutorService: JobExecutorService) : ExecuteHandler {
  val log: Logger = LoggerFactory.getLogger(this.javaClass)

  init {
    ExecuteHandlerFactory.register(ExecuteTypeEnum.JOB_HANDLER, this)
  }

  override fun execute(instance: JobInstance,
                       jobInfo: JobInfo,
                       triggerType: TriggerTypeEnum,
                       customExecuteParam: String?) {
    val jobId = jobInfo.jobId
    val chooseExecutors = chooseServers(jobInfo, jobId)
    val instanceId = instance.instanceId
    val executeParam = customExecuteParam ?: jobInfo.executeParam

    if (chooseExecutors.size == 1) {
      val jobExecutor = chooseExecutors[0]
      val jobParam = TaskParam()
      jobParam.jobId = jobId.toString()
      jobParam.instanceId = instanceId
      jobParam.executorHandler = jobInfo.executorHandler
      jobParam.executeParam = executeParam
      jobParam.blockStrategy = jobInfo.blockStrategy.name

      instance.executorInstance = jobExecutor.instanceId
      try {
        jobExecutor.execute(jobParam)
      } catch (e: Exception) {
        val errMsg = "${e.javaClass.name}:${e.message}"
        log.info("远程服务: {} 调用异常: {}", jobExecutor.instanceId, errMsg)
        throw VisibleException(CommonResMsg.INTERNAL_SERVER_ERROR, errMsg)
      }
    } else {
      for (executor in chooseExecutors) {
        val jobInstance = JobInstance.createInitialized()
        jobInstance.parentId = instanceId
        jobInstance.jobId = jobId
        jobInstance.executorId = jobInfo.executorId
        jobInstance.triggerType = triggerType
        jobInstance.schedulerInstance = instance.schedulerInstance
        jobInstance.executorInstance = executor.instanceId
        jobInstance.executorHandler = jobInfo.executorHandler
        jobInstance.executeParam = executeParam
        instanceService.saveInstance(jobInstance)

        val jobParam = TaskParam()
        jobParam.jobId = jobId.toString()
        jobParam.instanceId = jobInstance.instanceId
        jobParam.executorHandler = jobInfo.executorHandler
        jobParam.executeParam = executeParam
        jobParam.blockStrategy = jobInfo.blockStrategy.name
        try {
          executor.execute(jobParam)
        } catch (e: Exception) {
          val errMsg = "${e.javaClass.name}:${e.message}"
          instance.dispatchStatus = JobInstance.STATUS_FAIL
          instance.dispatchMsg = errMsg
          log.info("远程服务: {} 调用异常: {}", executor.instanceId, errMsg)
        }
      }
    }
  }

  /**
   * 选取远程执行器列表
   *
   * @author 宋志宗
   * @date 2020/8/29 22:00
   */
  private fun chooseServers(jobInfo: JobInfo, jobId: Long): List<TaskExecutor> {
    val executorId = jobInfo.executorId
    val executor = jobExecutorService.loadById(executorId)
    if (executor == null) {
      log.info("任务: {} 调度失败, 执行器: {} 不存在", jobId, executorId)
      throw VisibleException(CommonResMsg.NOT_FOUND, "执行器不存在")
    }

    val executorAppName = executor.appName
    if (StringUtils.isBlank(executorAppName)) {
      log.info("任务: {} 调度失败, 执行器: {} 应用名称为空", jobId, executorId)
      throw VisibleException("执行器应用名称为空")
    }

    if (StringUtils.isBlank(jobInfo.executorHandler)) {
      log.info("任务: {} 的执行处理器为空", jobId)
      throw VisibleException("executorHandler为空")
    }

    val serverHolder = lbFactory.getServerHolder(executorAppName)
    val reachableServers = serverHolder.reachableServers
    if (reachableServers.isEmpty()) {
      log.info("执行器: {} 当前没有可用的实例", executorAppName)
      throw VisibleException("执行器当前没有可用的实例")
    }
    val routeStrategy = jobInfo.routeStrategy
    return if (routeStrategy == RouteStrategyEnum.BROADCAST) {
      reachableServers
    } else {
      val loadBalancer = lbFactory
          .getLoadBalancer(executorAppName, routeStrategy.lbStrategy!!)
      val chooseServer = loadBalancer.chooseServer(jobId, reachableServers)
      if (chooseServer == null) {
        log.info("执行器: {} 选取实例为空", executorAppName)
        throw VisibleException("执行器选取实例为空")
      }
      listOf(chooseServer)
    }
  }
}