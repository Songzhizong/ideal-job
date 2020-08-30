package cn.sh.ideal.job.scheduler.core.dispatch

import cn.sh.ideal.job.common.constants.HandleStatusEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.utils.DateTimes
import cn.sh.ideal.job.common.exception.VisibleException
import cn.sh.ideal.job.common.message.payload.TaskCallback
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance
import cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerConfig
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

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
   * @param jobView             任务信息
   * @param triggerType         触发类型
   * @param customExecuteParam 自定义执行参数, 如果为空则使用任务默认配置
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/28 10:23 下午
   */
  fun dispatch(jobView: DispatchJobView, triggerType: TriggerTypeEnum, customExecuteParam: String?) {
    val executeType = jobView.executeType
    val handler = ExecuteHandlerFactory.getHandler(executeType)
    if (handler == null) {
      log.error("triggerType: {} 没有对应的执行处理器", triggerType)
      throw VisibleException("triggerType: $triggerType 没有对应的执行处理器")
    }
    val instance = JobInstance.createInitialized()
    instance.jobId = jobView.jobId
    instance.executorId = jobView.executorId
    instance.triggerType = triggerType
    instance.schedulerInstance = jobSchedulerConfig.ipPort
    instance.executorHandler = jobView.executorHandler
    instance.executeParam = customExecuteParam ?: jobView.executeParam
    instanceService.saveInstance(instance)
    try {
      handler.execute(instance, jobView, triggerType, customExecuteParam)
    } catch (e: Exception) {
      val errMsg = "${e.javaClass.name}:${e.message}"
      instance.dispatchStatus = JobInstance.STATUS_FAIL
      instance.dispatchMsg = errMsg
      log.info("任务: {} 调度异常: {}", jobView.jobId, errMsg)
    } finally {
      instanceService.updateDispatchInfo(instance)
    }
  }

  fun dispatchCallback(taskCallback: TaskCallback) {
    // cn.sh.ideal.job.scheduler.core.admin.repository.JobTriggerLogRepository.updateWhenTriggerCallback
    // triggerLog 设置参数务必和上述方法中的参数一致
    // 收到执行回调直接更新日志中变化的部分即可
    val handleStatus = taskCallback.handleStatus
    val triggerLog = JobInstance()
    triggerLog.instanceId = taskCallback.instanceId
    triggerLog.handleTime = taskCallback.handleTime
    triggerLog.finishedTime = taskCallback.finishedTime
    triggerLog.handleStatus = HandleStatusEnum.valueOfCode(handleStatus)
    triggerLog.result = taskCallback.handleMessage
    triggerLog.sequence = taskCallback.sequence
    triggerLog.updateTime = DateTimes.now()
    instanceService.updateWhenTriggerCallback(triggerLog)
  }
}