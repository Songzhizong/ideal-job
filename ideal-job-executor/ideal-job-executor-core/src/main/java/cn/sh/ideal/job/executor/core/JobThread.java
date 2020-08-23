package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.common.Destroyable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
public class JobThread extends Thread implements Destroyable {
  private static final Logger log = LoggerFactory.getLogger(JobThread.class);

  @Getter
  @Nonnull
  private final String jobId;
  @Getter
  @Nonnull
  private final IJobHandler jobHandler;
  private final BlockingQueue<ExecuteJobParam> jobQueue;
  private volatile boolean threadRunning = false;
  private volatile boolean jobRunning = false;
  private volatile boolean destroyed = false;
  @Setter
  private volatile boolean deprecated = false;
  @Setter
  private volatile boolean idleBeat = false;

  public JobThread(@Nonnull String jobId, @Nonnull IJobHandler jobHandler) {
    this.jobId = jobId;
    this.jobHandler = jobHandler;
    jobQueue = new ArrayBlockingQueue<>(100);
  }

  public void addJob(@Nonnull ExecuteJobParam param) {
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
    if (threadRunning) {
      // 防止无必要的重复调用
      return;
    }
    threadRunning = true;
    try {
      jobHandler.init();
    } catch (Exception exception) {
      String errMsg = exception.getClass().getName() + ":" + exception.getMessage();
      log.error("Handler init exception: {}", errMsg);
    }
    while (threadRunning) {
      jobRunning = false;
      ExecuteJobParam jobParam;
      ExecuteJobCallback callback = null;
      RemoteJobExecutor executor = null;
      long executeTime;
      try {
        jobParam = jobQueue.poll(3L, TimeUnit.SECONDS);
        if (jobParam == null) {
          if (deprecated) {
            destroy();
            return;
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

      executeTime = System.currentTimeMillis();
      try {
        jobHandler.execute(executorParams);
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
            callback.setHandleMessage("success");
          }
          callback.setTimeConsuming(System.currentTimeMillis() - executeTime);
          executor.executeJobCallback(callback);
        }
        if (idleBeat) {
          idleBeatNotice();
        }
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
    int queueSize = jobQueue.size();
    IdleBeatCallback idleBeatCallback = new IdleBeatCallback();
    idleBeatCallback.setJobId(jobId);
    if (jobRunning) {
      idleBeatCallback.setIdleLevel(queueSize + 1);
    } else {
      idleBeatCallback.setIdleLevel(queueSize);
    }
    LbServerHolder serverHolder = JobExecutor.getServerHolder();
    List<LbServer> reachableServers = serverHolder.getReachableServers();
    for (LbServer server : reachableServers) {

    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void destroy() {
    if (destroyed) {
      return;
    }
    destroyed = true;
    threadRunning = false;
  }
}
