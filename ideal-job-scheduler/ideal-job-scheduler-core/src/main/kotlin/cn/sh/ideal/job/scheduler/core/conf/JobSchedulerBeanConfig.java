package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.common.executor.JobExecutor;
import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.SimpleLbFactory;
import cn.sh.ideal.job.scheduler.core.generator.IDGenerator;
import cn.sh.ideal.job.scheduler.core.generator.ReactiveSpringRedisSnowFlakeInitializer;
import cn.sh.ideal.job.scheduler.core.generator.SnowFlake;
import cn.sh.ideal.job.scheduler.core.generator.SnowFlakeInitializer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.concurrent.*;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Configuration
public class JobSchedulerBeanConfig {
  private static final Logger log = LoggerFactory.getLogger(JobSchedulerBeanConfig.class);
  @Value("${spring.application.name}")
  private String applicationName;

  private final JobSchedulerProperties schedulerProperties;
  private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

  public JobSchedulerBeanConfig(JobSchedulerProperties schedulerProperties,
                                ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    this.schedulerProperties = schedulerProperties;
    this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
  }

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Bean
  public LbFactory<JobExecutor> lbFactory() {
    return new SimpleLbFactory<>();
  }

  @Bean
  public SnowFlakeInitializer snowFlakeInitializer() {
    return new ReactiveSpringRedisSnowFlakeInitializer(300,
        30, applicationName, reactiveStringRedisTemplate);
  }

  @Bean
  public IDGenerator idGenerator(SnowFlakeInitializer snowFlakeInitializer) {
    snowFlakeInitializer.init();
    SnowFlake snowFlake = SnowFlake.INSTANCE;
    if (log.isDebugEnabled()) {
      log.debug("test SnowFlake generate: " + snowFlake.generate());
    }
    return snowFlake;
  }

  @Bean
  public ExecutorService jobCallbackThreadPool() {
    int processors = Runtime.getRuntime().availableProcessors();
    ThreadPoolProperties properties = schedulerProperties.getExecuteJobCallbackPool();
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
}
