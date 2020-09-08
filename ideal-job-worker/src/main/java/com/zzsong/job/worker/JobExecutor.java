package com.zzsong.job.worker;

import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.worker.RemoteTaskWorker;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.loadbalancer.LbServerHolder;
import com.zzsong.job.common.loadbalancer.SimpleLbFactory;
import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.worker.handler.IJobHandler;
import com.zzsong.job.worker.handler.JobHandlerFactory;
import com.zzsong.job.worker.socket.ProtocolTypeEnum;
import com.zzsong.job.worker.socket.ReactorWebSocketRemoteTaskWorker;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zzsong.job.worker.socket.rsocket.RSocketRemoteTaskWorker;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
public class JobExecutor {
  private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);
  private static final String SCHEDULER_SERVER_NAME = "ideal-job-scheduler";
  private static final LbFactory<RemoteTaskWorker> lbFactory = new SimpleLbFactory<>();
  private static JobExecutor executor;

  private static final int CONNECT_TIME_OUT_MILLS = 2000;
  private static final long WRITE_TIME_OUT_MILLS = 1000;
  private static final long READ_TIME_OUT_MILLS = 120 * 1000;

  private ThreadPoolExecutor executorService;
  @Setter
  private String accessToken;
  @Setter
  private int weight;
  @Setter
  private ProtocolTypeEnum protocolType;
  @Setter
  private String schedulerAddresses;
  @Setter
  private String appName;
  @Setter
  private String workerIp;
  @Setter
  private int workerPort;
  @Setter
  private int corePoolSize = -1;
  @Setter
  private int maximumPoolSize = -1;
  @Setter
  private int poolQueueSize = 200;

  private volatile boolean destroyed;
  private final List<RemoteTaskWorker> remoteTaskWorkers = new ArrayList<>();

  @Nonnull
  public static JobExecutor getExecutor() {
    if (JobExecutor.executor == null) {
      throw new RuntimeException("JobExecutor 未初始化");
    }
    return JobExecutor.executor;
  }

  public JobExecutor() {
  }

  public void start() {
    if (StringUtils.isBlank(schedulerAddresses)) {
      log.error("调度器地址为空...");
      return;
    }
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    if (corePoolSize < 0) {
      corePoolSize = availableProcessors << 3;
    }
    if (maximumPoolSize < 1) {
      maximumPoolSize = availableProcessors << 5;
    }
    BlockingQueue<Runnable> workQueue;
    if (poolQueueSize < 1) {
      workQueue = new SynchronousQueue<>();
    } else {
      workQueue = new ArrayBlockingQueue<>(poolQueueSize);
    }
    executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, workQueue,
        new ThreadFactoryBuilder().setNameFormat("job-executor-pool-%d").build(),
        (r, executor) -> {
          log.error("任务执行线程池资源不足, 请尝试修改线程数配置, 当前核心线程数: {}, 最大线程数: {}, 队列长度: {}",
              corePoolSize, maximumPoolSize, poolQueueSize);
          throw new RejectedExecutionException("Task " + r.toString() +
              " rejected from Job executor thread pool");
        });
    executorService.allowCoreThreadTimeOut(true);
    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
    initRemoteWorks();
    JobExecutor.executor = this;
  }

  public void destroy() {
    if (!destroyed) {
      log.info("JobExecutor destroy.");
      destroyed = true;
      for (RemoteTaskWorker remoteExecutor : remoteTaskWorkers) {
        remoteExecutor.destroy();
      }
      executorService.shutdown();
    }
  }


  private void initRemoteWorks() {
    if (protocolType == ProtocolTypeEnum.RSOCKET) {
      initRSocketRemoteWorks();
    }
    if (protocolType == ProtocolTypeEnum.WEBSOCKET) {
      initWebsocketRemoteWorks();
    }
    LbServerHolder<RemoteTaskWorker> holder = getServerHolder();
    holder.addServers(remoteTaskWorkers);
  }

  private void initRSocketRemoteWorks() {
    final String[] addresses = StringUtils
        .split(schedulerAddresses, ",");
    for (String address : addresses) {
      String[] split = StringUtils.split(address, ":");
      if (split.length != 2) {
        log.error("RSocket地址配置错误: {}", address);
        throw new IllegalArgumentException("RSocket地址配置错误");
      }
      String ip = split[0];
      int port;
      try {
        port = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        log.error("RSocket地址配置错误: {}", address);
        throw new IllegalArgumentException("RSocket地址配置错误");
      }
      RSocketRemoteTaskWorker worker = new RSocketRemoteTaskWorker(ip, port);
      worker.setAppName(appName);
      worker.setWeight(weight);
      worker.setAccessToken(accessToken);
      worker.setWorkerIp(workerIp);
      worker.setWorkerPort(workerPort);
      worker.startWorker();
      remoteTaskWorkers.add(worker);
    }
  }

  private void initWebsocketRemoteWorks() {
    final String[] addresses = StringUtils
        .split(schedulerAddresses, ",");
    for (String address : addresses) {
      ReactorWebSocketRemoteTaskWorker worker
          = new ReactorWebSocketRemoteTaskWorker(address);
      worker.setAppName(appName);
      worker.setWorkerIp(workerIp);
      worker.setWorkerPort(workerPort);
      worker.setWeight(weight);
      worker.setAccessToken(accessToken);
      worker.setConnectTimeOutMills(CONNECT_TIME_OUT_MILLS);
      worker.setWriteTimeOutMills(WRITE_TIME_OUT_MILLS);
      worker.setReadTimeOutMills(READ_TIME_OUT_MILLS);
      worker.start();
      remoteTaskWorkers.add(worker);
    }
  }

  public LbServerHolder<RemoteTaskWorker> getServerHolder() {
    return lbFactory.getServerHolder(SCHEDULER_SERVER_NAME);
  }

  @Nullable
  public RemoteTaskWorker chooseRemoteJobExecutor() {
    return lbFactory.chooseServer(SCHEDULER_SERVER_NAME, null);
  }

  @Nullable
  public RemoteTaskWorker chooseRemoteJobExecutor(int retry) {
    if (retry < 1) {
      return chooseRemoteJobExecutor();
    }
    RemoteTaskWorker remoteJobExecutor = null;
    int i = -1;
    while (remoteJobExecutor == null || i < retry) {
      i++;
      remoteJobExecutor = chooseRemoteJobExecutor();
    }
    return remoteJobExecutor;
  }

  public Mono<Res<Void>> executeJob(@Nonnull TaskParam param) {
    final String handlerName = param.getExecutorHandler();
    final IJobHandler jobHandler = JobHandlerFactory.get(handlerName);
    if (jobHandler == null) {
      log.error("不存在此jobHandler: {}", handlerName);
      return Mono.just(Res.err("不存在此jobHandler: " + handlerName));
    }
    try {
      executorService.execute(() -> {
        TaskContext context = new TaskContext();
        context.setTaskParam(param);
        TaskContextHolder.putContext(context);

        String jobId = param.getJobId();
        long instanceId = param.getInstanceId();
        String executeParam = param.getExecuteParam();
        long executeTime = System.currentTimeMillis();

        TaskCallback callback = new TaskCallback();
        callback.setSequence(2);
        callback.setJobId(jobId);
        callback.setInstanceId(instanceId);
        callback.setHandleStatus(HandleStatusEnum.COMPLETE.getCode());
        callback.setHandleTime(executeTime);
        try {
          Object execute = jobHandler.execute(executeParam);
          if (execute != null) {
            String handleMessage = execute.toString();
            if (StringUtils.isNotBlank(handleMessage)) {
              callback.setHandleResult(handleMessage);
            }
          }
        } catch (Exception exception) {
          String errMsg = exception.getClass().getSimpleName() + ":" + exception.getMessage();
          log.info("Job execute exception: {}", errMsg);
          callback.setHandleStatus(HandleStatusEnum.ABNORMAL.getCode());
          callback.setHandleResult(errMsg);
        } finally {
          TaskContextHolder.removeContext();
          callback.setFinishedTime(System.currentTimeMillis());
          RemoteTaskWorker executor = chooseRemoteJobExecutor(2);
          // 任务完成时间尽量返回服务端
          if (executor != null) {
            executor.taskCallback(callback)
                .doOnNext(res -> {
                  // 失败了重试或者暂时缓存起来, 后续批量发送
                })
                .subscribe();
          } else {
            // 如果当前没有则暂时缓存起来
            log.warn("当前没有可用的RemoteJobExecutor");
          }
        }
      });
    } catch (Exception exception) {
      return Mono.error(exception);
    }
    return Mono.just(Res.success());
  }

  public void idleBeat(@Nonnull String jobId) {
    log.debug("{}", jobId);
  }
}
