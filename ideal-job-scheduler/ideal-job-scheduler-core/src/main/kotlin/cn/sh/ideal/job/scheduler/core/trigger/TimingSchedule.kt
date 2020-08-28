package cn.sh.ideal.job.scheduler.core.trigger

import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.service.JobService
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerProperties
import cn.sh.ideal.job.scheduler.core.utils.CronExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.sql.DataSource
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

/**
 * 定时调度器
 *
 * @author 宋志宗
 * @date 2020/8/27
 */
@Component
@WebListener
class TimingSchedule(
    private val dataSource: DataSource,
    private val jobService: JobService,
    private val jobTrigger: JobTrigger,
    private val cronJobThreadPool: ExecutorService,
    jobSchedulerProperties: JobSchedulerProperties) : ServletContextListener {
  companion object {
    val log: Logger = LoggerFactory.getLogger(TimingSchedule::class.java)
  }

  private val lockTable = jobSchedulerProperties.lockTable
  private val scheduleLockName = jobSchedulerProperties.scheduleLockName
  private val preReadCount = 500
  private val preReadMills = 5000L
  private val ringData = ConcurrentHashMap<Int, MutableList<JobInfo>>()
  private var scheduleThread: Thread? = null
  private var ringThread: Thread? = null

  @Volatile
  private var scheduleThreadToStop = false

  @Volatile
  private var ringThreadToStop = false

  // web容器销毁前先销毁定时调度器, 防止websocket连接先一步断开导致时间轮的消息没能发送出去
  override fun contextDestroyed(sce: ServletContextEvent) {
    stop()
  }

  @Suppress("DuplicatedCode")
  fun start() {
    scheduleThread = thread(start = true, isDaemon = true, name = "schedule-trigger-thread") {
      try {
        TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000)
      } catch (e: Exception) {
        if (!scheduleThreadToStop) {
          val errMessage = "${e.javaClass.name}:${e.message}"
          log.error("schedule-trigger-thread exception: {}", errMessage)
        }
      }
      while (!scheduleThreadToStop) {
        // Scan Job
        val start = System.currentTimeMillis()
        var preReadSuc = true
        dbLock {
          // 1、pre read
          val nowTime = System.currentTimeMillis()
          val jobList = jobService
              .loadScheduleJobs(nowTime + preReadMills, preReadCount)
          if (jobList.isEmpty()) {
            preReadSuc = false
          } else {
            for (jobInfo in jobList) {
              val nextTriggerTime = jobInfo.nextTriggerTime
              when {
                nowTime > nextTriggerTime + preReadMills -> {
                  log.warn("过期任务: {}", jobInfo.jobId)
                  refreshNextValidTime(jobInfo, Date())
                }
                nowTime > nextTriggerTime -> {
                  triggerJob(jobInfo)
                  refreshNextValidTime(jobInfo, Date())
                  val newNextTriggerTime = jobInfo.nextTriggerTime
                  if (jobInfo.jobStatus == JobInfo.JOB_START
                      && nowTime + preReadMills > newNextTriggerTime) {
                    val ringSecond = (newNextTriggerTime / 1000 % 60).toInt()
                    pushTimeRing(ringSecond, jobInfo)
                    refreshNextValidTime(jobInfo, Date(newNextTriggerTime))
                  }
                }
                else -> {
                  val ringSecond = (nextTriggerTime / 1000 % 60).toInt()
                  pushTimeRing(ringSecond, jobInfo)
                  refreshNextValidTime(jobInfo, Date(nextTriggerTime))
                }
              }
            }
            // 更新任务信息
            jobService.batchUpdateTriggerInfo(jobList)
          }
        }

        val cost = System.currentTimeMillis() - start
        if (cost < 1000) {
          // 如果未来preReadMills秒都没有数据, 那就休眠最多preReadMills秒, 反之最多休眠1秒
          val sleepMills = (if (preReadSuc) 1000 else preReadMills) - (System.currentTimeMillis() % 1000)
          try {
            TimeUnit.MILLISECONDS.sleep(sleepMills)
          } catch (e: Exception) {
            if (!scheduleThreadToStop) {
              val errMessage = "${e.javaClass.name}:${e.message}"
              log.error("schedule-trigger-thread exception: {}", errMessage)
            }
          }
        }
      }
      log.info("schedule-trigger-thread stop")
    }
    ringThread = thread(start = true, isDaemon = true, name = "schedule-trigger-ring-thread") {
      try {
        TimeUnit.MILLISECONDS.sleep(1000 - (System.currentTimeMillis() % 1000))
      } catch (e: Exception) {
        if (!scheduleThreadToStop) {
          val errMessage = "${e.javaClass.name}:${e.message}"
          log.error("schedule-trigger-thread exception: {}", errMessage)
        }
      }
      while (!ringThreadToStop) {
        val nowSecond = Calendar.getInstance().get(Calendar.SECOND)
        for (i in 0..1) {
          val tmpData = ringData.remove((nowSecond + 60 - i) % 60)
          tmpData?.onEach { triggerJob(it) }?.clear()
        }
        try {
          TimeUnit.MILLISECONDS.sleep(1000 - (System.currentTimeMillis() % 1000))
        } catch (e: Exception) {
          if (!scheduleThreadToStop) {
            val errMessage = "${e.javaClass.name}:${e.message}"
            log.error("schedule-trigger-thread exception: {}", errMessage)
          }
        }
      }
      log.info("schedule-trigger-ring-thread stop")
    }
  }

  private inline fun dbLock(block: () -> Unit) {
    var connection: Connection? = null
    var tempAutoCommit = false
    var preparedStatement: PreparedStatement? = null
    try {
      connection = dataSource.connection
      tempAutoCommit = connection.autoCommit
      connection.autoCommit = false
      @Suppress("SqlResolve")
      val sql = "select * from $lockTable where lock_name = '$scheduleLockName' for update"
      preparedStatement = connection
          .prepareStatement(sql)
      preparedStatement.execute()
      block.invoke()
    } catch (e: Exception) {
      val errMessage = "${e.javaClass.name}:${e.message}"
      log.warn("schedule-trigger-thread exception: {}", errMessage)
    } finally {
      try {
        preparedStatement?.close()
      } catch (e: Exception) {
        e.printStackTrace()
      }
      if (connection != null) {
        try {
          connection.commit()
        } catch (e: Exception) {
          e.printStackTrace()
        }
        try {
          connection.autoCommit = tempAutoCommit
        } catch (e: Exception) {
          e.printStackTrace()
        }
        try {
          connection.close()
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  private fun pushTimeRing(ringSecond: Int, jobInfo: JobInfo) {
    val list = ringData.computeIfAbsent(ringSecond) { ArrayList() }
    list.add(jobInfo)
  }

  private fun triggerJob(jobInfo: JobInfo) {
    val jobId = jobInfo.jobId
    val triggerParam = TriggerParam()
    triggerParam.jobId = jobId
    triggerParam.executorId = jobInfo.executorId
    triggerParam.triggerType = TriggerTypeEnum.CRON
    triggerParam.executorHandler = jobInfo.executorHandler
    triggerParam.executorParam = jobInfo.executorParam
    triggerParam.routeStrategy = jobInfo.routeStrategy
    triggerParam.blockStrategy = jobInfo.blockStrategy
    triggerParam.retryCount = jobInfo.retryCount
    cronJobThreadPool.execute {
      try {
        jobTrigger.trigger(triggerParam)
      } catch (e: Exception) {
        val errMsg = "${e.javaClass.name}:${e.message}"
        log.info("任务调度出现异常, jobId={}, message: {}", jobId, errMsg)
      }
    }
  }

  private fun refreshNextValidTime(jobInfo: JobInfo, date: Date) {
    val cron = jobInfo.cron
    val nextValidTimeAfter = CronExpression(cron).getNextValidTimeAfter(date)
    if (nextValidTimeAfter != null) {
      jobInfo.lastTriggerTime = jobInfo.nextTriggerTime
      jobInfo.nextTriggerTime = nextValidTimeAfter.time
    } else {
      jobInfo.jobStatus = JobInfo.JOB_STOP
      jobInfo.lastTriggerTime = 0
      jobInfo.nextTriggerTime = 0
    }
  }

  @Suppress("DuplicatedCode")
  fun stop() {
    if (scheduleThreadToStop || ringThreadToStop) {
      return
    }
    // 1、stop schedule
    scheduleThreadToStop = true
    try {
      TimeUnit.SECONDS.sleep(1)
    } catch (e: InterruptedException) {
      val errMessage = "${e.javaClass.name}:${e.message}"
      log.error("TimingSchedule stop exception: {}", errMessage)
    }
    if (scheduleThread?.state != Thread.State.TERMINATED) {
      // interrupt and wait
      scheduleThread?.interrupt()
      try {
        scheduleThread?.join()
      } catch (e: InterruptedException) {
        val errMessage = "${e.javaClass.name}:${e.message}"
        log.error("TimingSchedule stop exception: {}", errMessage)
      }
    }

    // if has ring data

    // if has ring data
    var hasRingData = false
    if (!ringData.isEmpty()) {
      for (second in ringData.keys) {
        val tmpData = ringData[second]
        if (tmpData != null && tmpData.size > 0) {
          hasRingData = true
          break
        }
      }
    }
    if (hasRingData) {
      try {
        TimeUnit.SECONDS.sleep(8)
      } catch (e: InterruptedException) {
        val errMessage = "${e.javaClass.name}:${e.message}"
        log.error("TimingSchedule stop exception: {}", errMessage)
      }
    }

    ringThreadToStop = true
    try {
      TimeUnit.SECONDS.sleep(1)
    } catch (e: InterruptedException) {
      val errMessage = "${e.javaClass.name}:${e.message}"
      log.error("TimingSchedule stop exception: {}", errMessage)
    }
    if (ringThread?.state != Thread.State.TERMINATED) {
      ringThread?.interrupt()
      try {
        ringThread?.join()
      } catch (e: InterruptedException) {
        val errMessage = "${e.javaClass.name}:${e.message}"
        log.error("TimingSchedule stop exception: {}", errMessage)
      }
    }
    log.info("TimingSchedule stop")
  }
}
