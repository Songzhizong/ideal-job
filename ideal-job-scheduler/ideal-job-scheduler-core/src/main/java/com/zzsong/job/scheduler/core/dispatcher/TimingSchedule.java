package com.zzsong.job.scheduler.core.dispatcher;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.scheduler.core.pojo.JobInfo;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.service.JobService;
import com.zzsong.job.scheduler.core.utils.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 宋志宗 on 2020/9/3
 */
@Component
public class TimingSchedule implements SmartInitializingSingleton {
  public static final Logger log = LoggerFactory.getLogger(TimingSchedule.class);
  private static final String LOCK_SQL
      = "select lock_name from ideal_job_lock where lock_name = 'schedule_lock' for update";
  private static final int preReadCount = 500;
  private static final long preReadMills = 5000L;

  private final ConcurrentMap<Integer, List<JobView>> ringData
      = new ConcurrentHashMap<>();
  private Thread scheduleThread;
  private Thread ringThread;
  private volatile boolean scheduleThreadToStop = false;
  private volatile boolean ringThreadToStop = false;

  @Nonnull
  private final DataSource dataSource;
  @Nonnull
  private final JobService jobService;
  @Nonnull
  private final JobDispatcher jobDispatcher;
  @Nonnull
  private final ExecutorService blockThreadPool;

  public TimingSchedule(@Nonnull DataSource dataSource,
                        @Nonnull JobService jobService,
                        @Nonnull ClusterNode jobDispatcher,
                        @Nonnull ExecutorService blockThreadPool) {
    this.dataSource = dataSource;
    this.jobService = jobService;
    this.jobDispatcher = jobDispatcher;
    this.blockThreadPool = blockThreadPool;
  }

  public void start() {
    scheduleThread = new Thread(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(5000 - (System.currentTimeMillis() % 1000));
      } catch (InterruptedException e) {
        if (!scheduleThreadToStop) {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.error("schedule-trigger-thread exception: {}", errMsg);
        }
      }
      while (!scheduleThreadToStop) {
        long start = System.currentTimeMillis();
        AtomicBoolean preReadSuc = new AtomicBoolean(true);
        dbLock(() -> {
          long nowTime = System.currentTimeMillis();
          List<JobView> jobViews = jobService
              .loadScheduleJobViews(nowTime + preReadMills, preReadCount)
              .collectList().block();
          if (jobViews == null || jobViews.isEmpty()) {
            preReadSuc.set(false);
          } else {
            Date nowDate = new Date();
            for (JobView jobView : jobViews) {
              long nextTriggerTime = jobView.getNextTriggerTime();
              if (nowTime > nextTriggerTime + preReadMills) {
                log.warn("过期任务: {}", jobView.getJobId());
                refreshNextValidTime(jobView, nowDate);
              } else if (nowTime >= nextTriggerTime) {
                triggerJob(jobView);
                refreshNextValidTime(jobView, nowDate);
                long newNextTriggerTime = jobView.getNextTriggerTime();
                if (jobView.getJobStatus() == JobInfo.JOB_START
                    && nowTime + preReadMills > newNextTriggerTime) {
                  int ringSecond = (int) (newNextTriggerTime / 1000 % 60);
                  pushTimeRing(ringSecond, jobView);
                  refreshNextValidTime(jobView, new Date(newNextTriggerTime));
                }
              } else {
                int ringSecond = (int) (nextTriggerTime / 1000 % 60);
                pushTimeRing(ringSecond, jobView);
                refreshNextValidTime(jobView, new Date(nextTriggerTime));
              }
            }
            // 更新任务信息
            jobService.batchUpdateTriggerInfo(jobViews).block();
          }
        });
        long cost = System.currentTimeMillis() - start;
        if (cost < 1000) {
          // 如果未来preReadMills秒都没有数据, 那就休眠最多preReadMills秒, 反之最多休眠1秒
          long sleepMills = (preReadSuc.get() ? 1000 : preReadMills)
              - (System.currentTimeMillis() % 1000);
          try {
            TimeUnit.MILLISECONDS.sleep(sleepMills);
          } catch (InterruptedException e) {
            if (!scheduleThreadToStop) {
              String errMsg = e.getClass().getName() + ": " + e.getMessage();
              log.error("schedule-trigger-thread exception: {}", errMsg);
            }
          }
        }
      }
      log.info("schedule-trigger-thread stop");
    });
    scheduleThread.setDaemon(true);
    scheduleThread.setName("schedule-trigger-thread");
    scheduleThread.start();

    ringThread = new Thread(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(1000 - (System.currentTimeMillis() % 1000));
      } catch (InterruptedException e) {
        if (!scheduleThreadToStop) {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.error("schedule-trigger-thread exception: {}", errMsg);
        }
      }
      while (!ringThreadToStop) {
        int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
        for (int i = 0; i < 2; i++) {
          int second = (nowSecond + 60 - i) % 60;
          List<JobView> tmpData = ringData.remove(second);
          if (tmpData != null) {
            tmpData.forEach(this::triggerJob);
            tmpData.clear();
          }
        }
        try {
          TimeUnit.MILLISECONDS.sleep(1000 - (System.currentTimeMillis() % 1000));
        } catch (InterruptedException e) {
          if (!scheduleThreadToStop) {
            String errMsg = e.getClass().getName() + ": " + e.getMessage();
            log.error("schedule-trigger-thread exception: {}", errMsg);
          }
        }
      }
      log.info("schedule-trigger-ring-thread stop");
    });
    ringThread.setDaemon(true);
    ringThread.setName("schedule-trigger-ring-thread");
    ringThread.start();
  }

  public void stop() {
    if (scheduleThreadToStop || ringThreadToStop) {
      return;
    }
    // 1、stop schedule
    scheduleThreadToStop = true;
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      log.error("TimingSchedule stop exception: Interrupted");
    }
    if (scheduleThread != null
        && scheduleThread.getState() != Thread.State.TERMINATED) {
      scheduleThread.interrupt();
      try {
        scheduleThread.join();
      } catch (InterruptedException e) {
        log.error("TimingSchedule stop exception: scheduleThread.join interrupted");
      }
    }

    // if has ring data
    boolean hasRingData = false;
    if (!ringData.isEmpty()) {
      for (Integer second : ringData.keySet()) {
        List<JobView> viewList = ringData.get(second);
        if (viewList != null && viewList.size() > 0) {
          hasRingData = true;
          break;
        }
      }
    }
    if (hasRingData) {
      try {
        TimeUnit.SECONDS.sleep(8);
      } catch (InterruptedException e) {
        log.error("TimingSchedule stop exception: Interrupted");
      }
    }

    ringThreadToStop = true;
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      log.error("TimingSchedule stop exception: Interrupted");
    }
    if (ringThread != null
        && ringThread.getState() != Thread.State.TERMINATED) {
      ringThread.interrupt();
      try {
        ringThread.join();
      } catch (InterruptedException e) {
        log.error("TimingSchedule stop exception: ringThread.join interrupted");
      }
    }
    log.info("TimingSchedule stop");
  }

  private void dbLock(Runnable runnable) {
    Connection connection = null;
    boolean tempAutoCommit = false;
    PreparedStatement preparedStatement = null;
    try {
      connection = dataSource.getConnection();
      tempAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      preparedStatement = connection.prepareStatement(LOCK_SQL);
      preparedStatement.execute();
      runnable.run();
    } catch (Exception e) {
      e.printStackTrace();
      String errMsg = e.getClass().getName() + ": " + e.getMessage();
      log.warn("schedule-trigger-thread exception: {}", errMsg);
    } finally {
      if (connection != null) {
        try {
          connection.commit();
        } catch (SQLException throwable) {
          throwable.printStackTrace();
        }
        if (preparedStatement != null) {
          try {
            preparedStatement.close();
          } catch (SQLException exception) {
            exception.printStackTrace();
          }
        }
        try {
          connection.setAutoCommit(tempAutoCommit);
        } catch (SQLException exception) {
          exception.printStackTrace();
        }
        try {
          connection.close();
        } catch (SQLException exception) {
          exception.printStackTrace();
        }
      }
    }
  }

  private void pushTimeRing(int ringSecond, JobView jobView) {
    List<JobView> viewList
        = ringData.computeIfAbsent(ringSecond, k -> new ArrayList<>());
    viewList.add(jobView);
  }

  private void triggerJob(JobView jobView) {
    blockThreadPool.execute(() ->
        jobDispatcher.dispatch(jobView, TriggerTypeEnum.CRON, null)
            .doOnError(throwable -> log.info("dispatch exception: ", throwable))
            .subscribe()
    );
  }

  private void refreshNextValidTime(@Nonnull JobView jobView, @Nonnull Date date) {
    String cron = jobView.getCron();
    Date nextValidTimeAfter;
    try {
      nextValidTimeAfter = new CronExpression(cron).getNextValidTimeAfter(date);
    } catch (Exception e) {
      String errMsg = e.getClass().getName() + ": " + e.getMessage();
      log.info("cron解析异常, cron: {}, message: {}", cron, errMsg);
      nextValidTimeAfter = null;
    }
    if (nextValidTimeAfter != null) {
      jobView.setLastTriggerTime(jobView.getNextTriggerTime());
      jobView.setNextTriggerTime(nextValidTimeAfter.getTime());
    } else {
      jobView.setJobStatus(JobInfo.JOB_STOP);
      jobView.setLastTriggerTime(0);
      jobView.setNextTriggerTime(0);
    }
  }

  @Override
  public void afterSingletonsInstantiated() {
    this.start();
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }
}
