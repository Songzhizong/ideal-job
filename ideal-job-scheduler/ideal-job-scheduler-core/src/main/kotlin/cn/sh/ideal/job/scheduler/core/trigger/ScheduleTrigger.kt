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
import javax.sql.DataSource
import kotlin.collections.ArrayList

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
@Component
class ScheduleTrigger(private val dataSource: DataSource,
                      private val jobService: JobService,
                      private val jobTrigger: JobTrigger,
                      private val cronJobThreadPool: ExecutorService,
                      jobSchedulerProperties: JobSchedulerProperties) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(ScheduleTrigger::class.java)
  }


  private val lockTable = jobSchedulerProperties.lockTable
  private val scheduleLockName = jobSchedulerProperties.scheduleLockName
  private val preReadCount = 500
  private val preReadMills = 5000L
  private val ringData = ConcurrentHashMap<Int, MutableList<Long>>()
  private var scheduleThread: Thread? = null
  private var ringThread: Thread? = null

  @Volatile
  private var scheduleThreadToStop = false

  @Volatile
  private var ringThreadToStop = false

  fun start() {
    scheduleThread = Thread {
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
        var connection: Connection? = null
        var autoCommit = false
        var preparedStatement: PreparedStatement? = null
        try {
          connection = dataSource.connection
          autoCommit = connection.autoCommit
          @Suppress("SqlResolve")
          val sql = "select * from $lockTable where lock_name = '$scheduleLockName' for update"
          preparedStatement = connection
              .prepareStatement(sql)
          preparedStatement.execute()

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
                    pushTimeRing(ringSecond, jobInfo.jobId)
                    refreshNextValidTime(jobInfo, Date(newNextTriggerTime))
                  }
                }
                else -> {
                  val ringSecond = (nextTriggerTime / 1000 % 60).toInt()
                  pushTimeRing(ringSecond, jobInfo.jobId)
                  refreshNextValidTime(jobInfo, Date(nextTriggerTime))
                }
              }
            }
            // 更新任务信息
            jobService.batchUpdate(jobList)
          }


        } catch (e: Exception) {
          if (!scheduleThreadToStop) {
            val errMessage = "${e.javaClass.name}:${e.message}"
            log.error("schedule-trigger-thread exception: {}", errMessage)
          }
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
              connection.autoCommit = autoCommit
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
        val cost = System.currentTimeMillis() - start
        if (cost < 1000) {
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
    scheduleThread!!.isDaemon = true
    scheduleThread!!.name = "schedule-trigger-thread"
    scheduleThread!!.start()

    ringThread = Thread {
      try {
        TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000)
      } catch (e: Exception) {
        if (!scheduleThreadToStop) {
          val errMessage = "${e.javaClass.name}:${e.message}"
          log.error("schedule-trigger-thread exception: {}", errMessage)
        }
      }
      while (!ringThreadToStop) {

      }
    }
    ringThread!!.isDaemon = true
    ringThread!!.name = "schedule-trigger-ring-thread"
    ringThread!!.start()
  }

  private fun pushTimeRing(ringSecond: Int, jobId: Long) {
    val list = ringData.computeIfAbsent(ringSecond) { ArrayList() }
    list.add(jobId)
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
}
