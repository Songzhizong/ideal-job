package cn.sh.ideal.job.scheduler.core.dispatch

import cn.sh.ideal.job.common.constants.HandleStatusEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.exception.VisibleException
import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerConfig
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
@Component
class JobDispatch(private val instanceService: JobInstanceService,
                  private val jobSchedulerConfig: JobSchedulerConfig) {
  val log: Logger = LoggerFactory.getLogger(this.javaClass)

  /**
   * 任务调度
   *
   * @param jobInfo             任务信息
   * @param triggerType         触发类型
   * @param customExecutorParam 自定义执行参数, 如果为空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/28 10:23 下午
   */
  fun dispatch(jobInfo: JobInfo, triggerType: TriggerTypeEnum, customExecutorParam: String?) {
    val executeType = jobInfo.executeType
    val handler = ExecuteHandlerFactory.getHandler(executeType)
    if (handler == null) {
      log.error("triggerType: {} 没有对应的执行处理器", triggerType)
      throw VisibleException("triggerType: $triggerType 没有对应的执行处理器")
    }
    val instance = JobInstance.createInitialized()
    instance.jobId = jobInfo.jobId
    instance.executorId = jobInfo.executorId
    instance.triggerType = triggerType
    instance.schedulerInstance = jobSchedulerConfig.ipPort
    instance.executorHandler = jobInfo.executorHandler
    instance.executorParam = customExecutorParam ?: jobInfo.executorParam
    instanceService.saveInstance(instance)
    try {
      handler.execute(instance, jobInfo, triggerType, customExecutorParam)
    } catch (e: Exception) {
      instance.dispatchStatus = JobInstance.STATUS_FAIL
      instance.dispatchMsg = e.message ?: e.javaClass.name
      instanceService.updateDispatchInfo(instance)
    }
  }

  fun dispatchCallback(executeJobCallback: ExecuteJobCallback) {
    val sequence = executeJobCallback.sequence
//    val jobId = executeJobCallback.jobId
    val triggerId = executeJobCallback.triggerId
    val handleStatus = executeJobCallback.handleStatus
    val handleMessage = executeJobCallback.handleMessage
    val handleTime = executeJobCallback.handleTime

    // cn.sh.ideal.job.scheduler.core.admin.repository.JobTriggerLogRepository.updateWhenTriggerCallback
    // triggerLog 设置参数务必和上述方法中的参数一致
    // 收到执行回调直接更新日志中变化的部分即可
    val triggerLog = JobInstance()
    triggerLog.instanceId = triggerId
    triggerLog.handleTime = handleTime
    triggerLog.handleStatus = HandleStatusEnum.valueOfCode(handleStatus)
    triggerLog.result = handleMessage
    triggerLog.sequence = sequence
    triggerLog.updateTime = LocalDateTime.now()
    instanceService.updateWhenTriggerCallback(triggerLog)
  }
}