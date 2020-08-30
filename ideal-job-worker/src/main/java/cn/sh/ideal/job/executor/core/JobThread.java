//package cn.sh.ideal.job.executor.core;
//
//import cn.sh.ideal.job.common.constants.HandleStatusEnum;
//import cn.sh.ideal.job.common.executor.RemoteTaskExecutor;
//import cn.sh.ideal.job.common.loadbalancer.LbServer;
//import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
//import cn.sh.ideal.job.common.message.payload.TaskCallback;
//import cn.sh.ideal.job.common.message.payload.TaskParam;
//import cn.sh.ideal.job.common.message.payload.IdleBeatCallback;
//import cn.sh.ideal.job.executor.core.handler.IJobHandler;
//import lombok.Getter;
//import lombok.Setter;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nonnull;
//import java.util.List;
//import java.util.concurrent.*;
//
///**
// * 任务线程
// *
// * @author 宋志宗
// * @date 2020/8/22
// */
//public final class JobThread extends Thread {
//  private static final Logger log = LoggerFactory.getLogger(JobThread.class);
//  private static final long autoIdleBeatNoticeTime = 10 * 1000;
//  private static final int jobQueueSize = 1000;
//  private static final long autoDestroyMills = 5 * 60 * 1000;
//
//  @Getter
//  @Nonnull
//  private final String jobId;
//  @Getter
//  @Nonnull
//  private final IJobHandler jobHandler;
//  private final BlockingQueue<TaskParam> jobQueue;
//  private final ExecutorService idleBeatPool;
//
//  private volatile boolean start = false;
//  private volatile boolean jobRunning = false;
//  private volatile boolean destroyed = false;
//  /**
//   * 标识当前线程的丢弃状态, 如果为true, 当现有的任务全部执行完成就销毁此线程
//   */
//  @Setter
//  private volatile boolean deprecated = false;
//  @Setter
//  private volatile boolean idleBeat = false;
//  private volatile long lastIdleBeatNoticeTime = 0;
//  private volatile long lastExecuteTime = System.currentTimeMillis();
//
//  public JobThread(@Nonnull String jobId, @Nonnull IJobHandler jobHandler) {
//    this.jobId = jobId;
//    this.jobHandler = jobHandler;
//    jobQueue = new ArrayBlockingQueue<>(jobQueueSize);
//    idleBeatPool = new ThreadPoolExecutor(0, 1,
//        60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1),
//        runnable -> new Thread(runnable, "idle-beat-pool-" +
//            jobId + "-" + runnable.hashCode()),
//        new ThreadPoolExecutor.DiscardPolicy());
//    log.debug("新建任务线程, jobId: {}, jobHandler: {}", jobId, jobHandler.toString());
//  }
//
//  public void putJob(@Nonnull TaskParam param) {
//    if (destroyed) {
//      log.error("JobThread已被丢弃, 但仍然收到添加任务请求, jobId: {}", jobId);
//      return;
//    }
//    boolean offer = jobQueue.offer(param);
//    if (!offer) {
//      discardJobNotice(param, "任务队列已满, 新的任务未能执行");
//      log.info("任务队列已满, 新的任务未能执行");
//    }
//  }
//
//  private void discardJobNotice(TaskParam jobParam, @SuppressWarnings("SameParameterValue") String handleMessage) {
//    final TaskCallback callback = new TaskCallback();
//    callback.setSequence(1);
//    callback.setJobId(jobParam.getJobId());
//    callback.setInstanceId(jobParam.getInstanceId());
//    callback.setHandleStatus(HandleStatusEnum.DISCARD.getCode());
//    callback.setHandleMessage(handleMessage);
//    callback.setHandleTime(0);
//    final RemoteTaskExecutor executor = JobExecutor.getExecutor().chooseRemoteJobExecutor(1);
//    if (executor == null) {
//      log.warn("当前没有可用的RemoteJobExecutor");
//      return;
//    }
//    executor.taskCallback(callback);
//  }
//
//  @Override
//  public void run() {
//    if (start) {
//      // 防止无必要的重复调用
//      return;
//    }
//    start = true;
//    while (start) {
//      long currentTimeMillis = System.currentTimeMillis();
//      if (currentTimeMillis - lastIdleBeatNoticeTime > autoIdleBeatNoticeTime) {
//        idleBeatNotice();
//      }
//      jobRunning = false;
//      TaskParam jobParam;
//      TaskCallback endCallback = null;
//      RemoteTaskExecutor executor = null;
//      long executeTime;
//      try {
//        jobParam = jobQueue.poll(3L, TimeUnit.SECONDS);
//        if (jobParam == null) {
//          if (deprecated) {
//            // 任务已经全部执行完成, 销毁此线程
//            shutdown();
//            break;
//          }
//          if (currentTimeMillis - lastExecuteTime > autoDestroyMills) {
//            log.info("任务线程空闲时间超时, 注销此任务线程, jobId: {}", jobId);
//            JobThreadFactory.remove(this);
//            break;
//          }
//          continue;
//        }
//      } catch (InterruptedException e) {
//        log.info("poll jobParam throw InterruptedException: {}", e.getMessage());
//        continue;
//      }
//      jobRunning = true;
//      long triggerId = jobParam.getInstanceId();
//      String executeParam = jobParam.getExecuteParam();
//
//      lastExecuteTime = currentTimeMillis;
//      executeTime = currentTimeMillis;
//      try {
//        executor = JobExecutor.getExecutor().chooseRemoteJobExecutor();
//        if (executor == null) {
//          log.warn("当前没有可用的RemoteJobExecutor");
//        } else {
//          int sequence = 0;
//          // 任务开始执行, 发送回调消息
//          TaskCallback runningCallback = new TaskCallback();
//          runningCallback.setSequence(++sequence);
//          runningCallback.setJobId(jobId);
//          runningCallback.setInstanceId(triggerId);
//          runningCallback.setHandleStatus(HandleStatusEnum.RUNNING.getCode());
//          runningCallback.setHandleTime(executeTime);
//          executor.taskCallback(runningCallback);
//
//          // 任务结束回调的序列必须后一步生成, 只有这样才能保证结束回调的序列大于启动回调的序列值
//          endCallback = new TaskCallback();
//          endCallback.setSequence(++sequence);
//          endCallback.setJobId(jobId);
//          endCallback.setInstanceId(triggerId);
//          endCallback.setHandleTime(executeTime);
//        }
//      } catch (Exception exception) {
//        String errMsg = exception.getClass().getSimpleName() + ":" + exception.getMessage();
//        log.info("exception: {}", errMsg);
//      }
//      try {
//        Object execute = jobHandler.execute(executeParam);
//        if (execute != null && endCallback != null) {
//          String handleMessage = execute.toString();
//          if (StringUtils.isNotBlank(handleMessage)) {
//            endCallback.setHandleMessage(handleMessage);
//          }
//        }
//      } catch (Exception e) {
//        String errMsg = e.getClass().getSimpleName() + ":" + e.getMessage();
//        log.info("Job execute exception: {}", errMsg);
//        if (endCallback != null) {
//          endCallback.setHandleStatus(HandleStatusEnum.ABNORMAL.getCode());
//          endCallback.setHandleMessage(errMsg);
//        }
//      } finally {
//        jobRunning = false;
//        if (executor != null && endCallback != null) {
//          int handleStatus = endCallback.getHandleStatus();
//          if (handleStatus == -1) {
//            endCallback.setHandleStatus(HandleStatusEnum.COMPLETE.getCode());
//          }
//          if (StringUtils.isBlank(endCallback.getHandleMessage())) {
//            endCallback.setHandleMessage("Success");
//          }
//          executor.taskCallback(endCallback);
//        }
//        idleBeatNotice();
//      }
//    }
//  }
//
//  private void idleBeatNotice() {
//    // 如果任务线程被丢弃了, 就没必要发送空闲状态通知了
//    if (!deprecated && idleBeat) {
//      idleBeatPool.submit(() -> {
//        IdleBeatCallback idleBeatCallback = new IdleBeatCallback();
//        idleBeatCallback.setJobId(jobId);
//        idleBeatCallback.setIdleLevel(getJobCount());
//        LbServerHolder<RemoteTaskExecutor> serverHolder = JobExecutor.getExecutor().getServerHolder();
//        List<RemoteTaskExecutor> reachableServers = serverHolder.getReachableServers();
//        for (LbServer server : reachableServers) {
//          RemoteTaskExecutor executor = (RemoteTaskExecutor) server;
//          executor.idleBeatCallback(idleBeatCallback);
//        }
//        lastIdleBeatNoticeTime = System.currentTimeMillis();
//      });
//    }
//  }
//
//  public int getJobCount() {
//    int queueSize = jobQueue.size();
//    if (jobRunning) {
//      return queueSize + 1;
//    } else {
//      return queueSize;
//    }
//  }
//
//
//  public void shutdown() {
//    if (destroyed) {
//      return;
//    }
//    destroyed = true;
//    start = false;
//    idleBeatPool.shutdown();
//    this.interrupt();
//  }
//}
