package cn.sh.ideal.job.scheduler.core.dispatch

import cn.sh.ideal.job.common.constants.HandleStatusEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.entity.JobTriggerLog
import cn.sh.ideal.job.scheduler.core.admin.service.JobTriggerLogService
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory
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
class JobDispatch(private val triggerLogService: JobTriggerLogService) {
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
  fun dispatch(jobInfo: JobInfo, triggerType: TriggerTypeEnum, customExecutorParam: String?): Res<Void> {
    val executeType = jobInfo.executeType
    val handler = ExecuteHandlerFactory.getHandler(executeType)
    if (handler == null) {
      log.error("triggerType: {} 没有对应的触发处理器", triggerType)
      return Res.err("没有触发处理器")
    }
    return handler.execute(jobInfo, triggerType, customExecutorParam)
  }

  fun dispatchCallback(executeJobCallback: ExecuteJobCallback) {
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