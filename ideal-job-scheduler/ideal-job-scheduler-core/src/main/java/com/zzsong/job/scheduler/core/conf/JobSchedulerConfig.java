package com.zzsong.job.scheduler.core.conf;

import com.google.common.eventbus.EventBus;
import com.zzsong.job.common.cache.ReactiveCache;
import com.zzsong.job.common.cache.ReactiveRedisClient;
import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.loadbalancer.SimpleLbFactory;
import com.zzsong.job.common.utils.IpUtil;
import com.zzsong.job.scheduler.core.dispatcher.LocalClusterNode;
import com.zzsong.job.scheduler.core.dispatcher.cluster.ClusterRegistry;
import com.zzsong.job.scheduler.core.dispatcher.cluster.ClusterSocket;
import com.zzsong.job.scheduler.core.generator.IDGenerator;
import com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator;
import com.zzsong.job.scheduler.core.generator.ReactiveRedisRegisterSnowFlake;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 宋志宗 on 2020/8/20
 */
@Configuration
public class JobSchedulerConfig {
  private static final Logger log = LoggerFactory.getLogger(JobSchedulerConfig.class);
  @Value("${spring.application.name}")
  private String applicationName;

  private String ip = null;
  @Value("${server.port:8080}")
  private int port;
  private String ipPort = null;
  private final JobSchedulerProperties schedulerProperties;

  public JobSchedulerConfig(JobSchedulerProperties schedulerProperties) {
    this.schedulerProperties = schedulerProperties;
  }

  public String getIp() {
    if (ip != null) {
      return ip;
    }
    ip = IpUtil.getIp();
    return ip;
  }

  public int getPort() {
    return port;
  }

  public String getIpPort() {
    if (ipPort != null) {
      return ipPort;
    }
    final String ip = getIp();
    final int port = getPort();
    ipPort = ip + ":" + port;
    return ipPort;
  }

//    @Bean
//    public ServerEndpointExporter serverEndpointExporter() {
//        return new ServerEndpointExporter();
//    }

  @SuppressWarnings("UnstableApiUsage")
  @Bean
  public EventBus eventBus() {
    return new EventBus();
  }

  @SuppressWarnings("DuplicatedCode")
  @Bean
  public LbFactory<TaskWorker> lbFactory(@Nonnull ClusterRegistry registry,
                                         @Nonnull ClusterSocket clusterSocket,
                                         @Nonnull LocalClusterNode localClusterNode) {
    final SimpleLbFactory<TaskWorker> lbFactory = new SimpleLbFactory<>();
    lbFactory.registerEventListener((factory, event) -> {
      int reachableServerCount = event.getReachableServerCount();

      final Map<String, List<TaskWorker>> map = factory.getReachableServers();
      Map<String, List<String>> supportApps = new HashMap<>();
      map.forEach((appName, list) -> {
        List<String> instanceList = new ArrayList<>();
        if (list != null && list.size() > 0) {
          for (TaskWorker taskWorker : list) {
            instanceList.add(taskWorker.getInstanceId());
          }
        }
        if (instanceList.size() > 0) {
          supportApps.put(appName, instanceList);
        }
      });
      if (reachableServerCount == 0) {
        registry.refreshNode(localClusterNode, supportApps);
      }
      clusterSocket.refreshNodeNotice(supportApps);
    });
    return lbFactory;
  }

  @Bean
  public IDGenerator idGenerator(ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    ReactiveRedisRegisterSnowFlake snowFlake
        = new ReactiveRedisRegisterSnowFlake(applicationName, reactiveStringRedisTemplate);
    log.debug("Test SnowFlake: {}", snowFlake.generate());
    JpaIdentityGenerator.setIDGenerator(snowFlake);
    return snowFlake;
  }

  @Bean
  public ReactiveCache reactiveCache(@Nonnull ReactiveStringRedisTemplate template) {
    return new ReactiveRedisClient(template);
  }

  @Bean
  public ExecutorService blockThreadPool() {
    int processors = Runtime.getRuntime().availableProcessors();
    ThreadPoolProperties properties = schedulerProperties.getBlockPool();
    int corePoolSize = properties.getCorePoolSize();
    if (corePoolSize < 0) {
      corePoolSize = processors << 1;
    }
    int maximumPoolSize = properties.getMaximumPoolSize();
    if (maximumPoolSize < 1) {
      maximumPoolSize = processors << 4;
    }
    BlockingQueue<Runnable> workQueue;
    int workQueueSize = properties.getWorkQueueSize();
    if (workQueueSize < 1) {
      workQueue = new SynchronousQueue<>();
    } else {
      workQueue = new ArrayBlockingQueue<>(workQueueSize);
    }
    ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, workQueue,
        new ThreadFactoryBuilder().setNameFormat("job-callback-pool-%d").build(),
        (r, executor) -> {
          throw new RejectedExecutionException("Task " + r.toString() +
              " rejected from jobCallbackThreadPool");
        });
    pool.allowCoreThreadTimeOut(true);
    return pool;
  }

  @Bean
  public Scheduler blockScheduler(ExecutorService blockThreadPool) {
    return Schedulers.fromExecutorService(blockThreadPool, "blockScheduler");
  }
}
