package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.common.constants.BlockStrategyEnum;
import cn.sh.ideal.job.common.constants.HandleStatusEnum;
import cn.sh.ideal.job.common.executor.RemoteJobExecutor;
import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback;
import cn.sh.ideal.job.common.message.payload.ExecuteJobParam;
import cn.sh.ideal.job.common.message.payload.IdleBeatCallback;
import cn.sh.ideal.job.executor.core.handler.IJobHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.*;

/**
 * 任务线程
 *
 * @author 宋志宗
 * @date 2020/8/22
 */
public final class JobThread extends Thread {
  private static final Logger log = LoggerFactory.getLogger(JobThread.class);
  private static final long autoIdleBeatNoticeTime = 10 * 1000;
  private static final int jobQueueSize = 1000;
  @SuppressWarnings("PointlessArithmeticExpression")
  private static final long autoDestroyMills = 1 * 60 * 60 * 1000;

  @Getter
  @Nonnull
  private final String jobId;
  @Getter
  @Nonnull
  private final IJobHandler jobHandler;
  private final BlockingQueue<ExecuteJobParam> jobQueue;
  private final ExecutorService idleBeatPool;

  private volatile boolean start = false;
  private volatile boolean jobRunning = false;
  private volatile boolean destroyed = false;
  /**
   * 标识当前线程的丢弃状态, 如果为true, 当现有的任务全部执行完成就销毁此线程
   */
  @Setter
  private volatile boolean deprecated = false;
  @Setter
  private volatile boolean idleBeat = false;
  private volatile long lastIdleBeatNoticeTime = 0;
  private volatile long lastExecuteTime = System.currentTimeMillis();

  public JobThread(@Nonnull String jobId, @Nonnull IJobHandler jobHandler) {
    this.jobId = jobId;
    this.jobHandler = jobHandler;
    jobQueue = new ArrayBlockingQueue<>(jobQueueSize);
    idleBeatPool = new ThreadPoolExecutor(0, 1,
        60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1),
        runnable -> new Thread(runnable, "idle-beat-pool-" + jobId + "-" + runnable.hashCode()),
        new ThreadPoolExecutor.DiscardPolicy());
  }

  public void addJob(@Nonnull ExecuteJobParam param) {
    if (!start || destroyed) {
      log.error("JobThread已被丢弃, 但仍然收到添加任务请求, jobId: {}", jobId);
      return;
    }
    int strategy = param.getBlockStrategy();
    if (strategy == BlockStrategyEnum.DISCARD_LATER.getCode()) {
      // 丢弃后续调度
      // todo 执行回调
      return;
    } else if (strategy == BlockStrategyEnum.COVER_EARLY.getCode()) {
      jobQueue.clear();
      // todo 执行回调
    }
    jobQueue.offer(param);
  }

  @Override
  public void run() {
    if (start) {
      // 防止无必要的重复调用
      return;
    }
    start = true;
    try {
      jobHandler.init();
    } catch (Exception exception) {
      String errMsg = exception.getClass().getName() + ":" + exception.getMessage();
      log.error("Handler init exception: {}", errMsg);
    }
    while (start) {
      long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis - lastIdleBeatNoticeTime > autoIdleBeatNoticeTime) {
        idleBeatNotice();
      }
      jobRunning = false;
      ExecuteJobParam jobParam;
      ExecuteJobCallback callback = null;
      RemoteJobExecutor executor = null;
      long executeTime;
      try {
        jobParam = jobQueue.poll(3L, TimeUnit.SECONDS);
        if (jobParam == null) {
          if (deprecated) {
            // 任务已经全部执行完成, 销毁此线程
            shutdown();
            return;
          }
          if (currentTimeMillis - lastExecuteTime > autoDestroyMills) {
            log.info("任务线程空闲时间超时, 注销此任务线程, jobId: {}", jobId);
            JobThreadFactory.remove(this);
          }
          continue;
        }
      } catch (InterruptedException e) {
        log.info("poll jobParam throw InterruptedException: {}", e.getMessage());
        continue;
      }
      jobRunning = true;
      long triggerId = jobParam.getTriggerId();
      String executorParams = jobParam.getExecutorParams();

      try {
        executor = JobExecutor.chooseRemoteJobExecutor();
        if (executor == null) {
          log.warn("当前没有可用的RemoteJobExecutor");
        } else {
          callback = new ExecuteJobCallback();
          callback.setJobId(jobId);
          callback.setTriggerId(triggerId);

          // 任务开始执行, 发送回调消息
          ExecuteJobCallback runningCallback = new ExecuteJobCallback();
          runningCallback.setJobId(jobId);
          runningCallback.setTriggerId(triggerId);
          runningCallback.setHandleStatus(HandleStatusEnum.RUNNING.getCode());
          executor.executeJobCallback(runningCallback);
        }
      } catch (Exception exception) {
        String errMsg = exception.getClass().getSimpleName() + ":" + exception.getMessage();
        log.info("exception: {}", errMsg);
      }

      lastExecuteTime = currentTimeMillis;
      executeTime = currentTimeMillis;
      try {
        Object execute = jobHandler.execute(executorParams);
        if (execute != null && callback != null) {
          String handleMessage = execute.toString();
          if (StringUtils.isNotBlank(handleMessage)) {
            callback.setHandleMessage(handleMessage);
          }
        }
      } catch (Exception e) {
        String errMsg = e.getClass().getSimpleName() + ":" + e.getMessage();
        log.info("Job execute exception: {}", errMsg);
        if (callback != null) {
          callback.setHandleStatus(HandleStatusEnum.ABNORMAL.getCode());
          callback.setHandleMessage(errMsg);
        }
      } finally {
        jobRunning = false;
        if (executor != null && callback != null) {
          int handleStatus = callback.getHandleStatus();
          if (handleStatus == -1) {
            callback.setHandleStatus(HandleStatusEnum.COMPLETE.getCode());
          }
          if (StringUtils.isNotBlank(callback.getHandleMessage())) {
            callback.setHandleMessage("Success");
          }
          callback.setTimeConsuming(currentTimeMillis - executeTime);
          executor.executeJobCallback(callback);
        }
        idleBeatNotice();
      }
    }

    try {
      jobHandler.destroy();
    } catch (Exception exception) {
      String errMsg = exception.getClass().getName() + ":" + exception.getMessage();
      log.error("Handler destroy exception: {}", errMsg);
    }
  }

  private void idleBeatNotice() {
    // 如果任务线程被丢弃了, 就没必要发送空闲状态通知了
    if (!deprecated && idleBeat) {
      idleBeatPool.submit(() -> {
        IdleBeatCallback idleBeatCallback = new IdleBeatCallback();
        idleBeatCallback.setJobId(jobId);
        idleBeatCallback.setIdleLevel(getJobCount());
        LbServerHolder serverHolder = JobExecutor.getServerHolder();
        List<LbServer> reachableServers = serverHolder.getReachableServers();
        for (LbServer server : reachableServers) {
          RemoteJobExecutor executor = (RemoteJobExecutor) server;
          executor.idleBeatCallback(idleBeatCallback);
        }
        lastIdleBeatNoticeTime = System.currentTimeMillis();
      });
    }
  }

  public int getJobCount() {
    int queueSize = jobQueue.size();
    if (jobRunning) {
      return queueSize + 1;
    } else {
      return queueSize;
    }
  }


  public void shutdown() {
    if (destroyed) {
      return;
    }
    destroyed = true;
    start = false;
    idleBeatPool.shutdown();
    this.interrupt();
  }
}
