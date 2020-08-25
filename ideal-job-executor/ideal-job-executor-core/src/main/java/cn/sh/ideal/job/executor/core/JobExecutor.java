package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.common.Destroyable;
import cn.sh.ideal.job.common.executor.RemoteJobExecutor;
import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.loadbalancer.SimpleLbFactory;
import cn.sh.ideal.job.common.message.payload.ExecuteJobParam;
import cn.sh.ideal.job.executor.core.handler.IJobHandler;
import cn.sh.ideal.job.executor.core.handler.JobHandlerFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Getter
@Setter
public class JobExecutor implements Destroyable {
  private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);
  private static final String SCHEDULER_SERVER_NAME = "ideal-job-scheduler";
  private static final LbFactory<RemoteJobExecutor> lbFactory = new SimpleLbFactory<>();
  private String accessToken;
  private int weight;
  private String schedulerAddresses;
  private String appName;
  private String ip;
  private int port;
  private int connectTimeOutMills = 2000;
  private long writeTimeOutMills = 200;
  private long readTimeOutMills = 120 * 1000;
  private volatile boolean destroyed;
  private final List<RemoteJobExecutor> remoteExecutors = new ArrayList<>();

  public JobExecutor() {
  }

  public void start() {
    if (StringUtils.isBlank(schedulerAddresses)) {
      log.error("调度器地址为空...");
      return;
    }
    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
    initRemoteExecutors();
  }

  @Override
  public void destroy() {
    if (!destroyed) {
      log.info("JobExecutor destroy.");
      destroyed = true;
      for (RemoteJobExecutor remoteExecutor : remoteExecutors) {
        remoteExecutor.destroy();
      }
    }
  }


  private void initRemoteExecutors() {
    final String[] addresses = StringUtils.split(schedulerAddresses, ",");
//    List<RemoteJobExecutor> remoteExecutors = new ArrayList<>();
    for (String address : addresses) {
      ReactorWebSocketRemoteJobExecutor executor = new ReactorWebSocketRemoteJobExecutor(address);
      executor.setAppName(appName);
      executor.setIp(ip);
      executor.setPort(port);
      executor.setWeight(weight);
      executor.setAccessToken(accessToken);
      executor.setConnectTimeOutMills(connectTimeOutMills);
      executor.setWriteTimeOutMills(writeTimeOutMills);
      executor.setReadTimeOutMills(readTimeOutMills);
      executor.start();
      remoteExecutors.add(executor);
    }
    LbServerHolder<RemoteJobExecutor> holder = getServerHolder();
    holder.addServers(remoteExecutors, true);
//    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      for (RemoteJobExecutor remoteExecutor : remoteExecutors) {
//        remoteExecutor.destroy();
//      }
//    }));
  }

  // ---------------------------------- static ~ ~ ~
  public static LbServerHolder<RemoteJobExecutor> getServerHolder() {
    return lbFactory.getServerHolder(SCHEDULER_SERVER_NAME);
  }

  @Nullable
  public static RemoteJobExecutor chooseRemoteJobExecutor() {
    return lbFactory.chooseServer(SCHEDULER_SERVER_NAME, null);
  }

  public static void executeJob(@Nonnull ExecuteJobParam param) {
    final String jobId = param.getJobId();
    final String handlerName = param.getExecutorHandler();
    final IJobHandler jobHandler = JobHandlerFactory.get(handlerName);
    if (jobHandler == null) {
      log.error("不存在此jobHandler: {}", handlerName);
      return;
    }
    JobThread jobThread = JobThreadFactory.computeIfAbsent(jobId, k -> new JobThread(jobId, jobHandler));
    final IJobHandler currentJobHandler = jobThread.getJobHandler();
    // 如果JobHandler发生了变更, 则将原有的jobThread弃用并注册新的jobThread
    if (jobHandler != currentJobHandler) {
      jobThread.setDeprecated(true);
      jobThread = new JobThread(jobId, jobHandler);
      JobThreadFactory.register(jobId, jobThread);
    }
    jobThread.putJob(param);
  }

  public static void idleBeat(@Nonnull String jobId) {
    final JobThread jobThread = JobThreadFactory.get(jobId);
    if (jobThread != null) {
      jobThread.setIdleBeat(true);
    }
  }
}
