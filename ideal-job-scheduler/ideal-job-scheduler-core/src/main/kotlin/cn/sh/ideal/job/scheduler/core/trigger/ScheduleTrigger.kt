package cn.sh.ideal.job.scheduler.core.trigger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

/**
 * @author 宋志宗
 * @date 2020/8/27
 */
@Component
object ScheduleTrigger {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)
  private val ringData = ConcurrentHashMap<Int, List<Long>>()

  lateinit var dataSource: DataSource
  lateinit var lockTable: String
  lateinit var scheduleLockName: String
  private val preReadCount = 500
  private val preReadMills = 5000L
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
          preparedStatement = connection
              .prepareStatement("select * from $lockTable where lock_name = '$scheduleLockName' for update")
          preparedStatement.execute()


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

    }
    ringThread!!.isDaemon = true
    ringThread!!.name = "schedule-trigger-ring-thread"
    ringThread!!.start()
  }

}
