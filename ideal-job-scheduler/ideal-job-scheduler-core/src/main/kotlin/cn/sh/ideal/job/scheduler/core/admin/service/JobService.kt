package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.res.CommonResMsg
import cn.sh.ideal.job.common.res.Res
import cn.sh.ideal.job.scheduler.core.admin.repository.JobInfoRepository
import cn.sh.ideal.job.scheduler.core.trigger.JobTrigger
import cn.sh.ideal.job.scheduler.core.trigger.TriggerParam
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Service
class JobService(private val jobTrigger: JobTrigger,
                 private val jobExecutorService: JobExecutorService,
                 private val jobInfoRepository: JobInfoRepository) {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)

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
    val executor = jobExecutorService.findById(executorId)
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