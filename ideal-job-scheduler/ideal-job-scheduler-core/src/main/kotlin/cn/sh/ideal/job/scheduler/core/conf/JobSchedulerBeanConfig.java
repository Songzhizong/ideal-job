package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.common.executor.JobExecutor;
import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.SimpleLbFactory;
import cn.sh.ideal.job.scheduler.core.generator.IDGenerator;
import cn.sh.ideal.job.scheduler.core.generator.ReactiveSpringRedisSnowFlakeInitializer;
import cn.sh.ideal.job.scheduler.core.generator.SnowFlake;
import cn.sh.ideal.job.scheduler.core.generator.SnowFlakeInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Configuration
public class JobSchedulerBeanConfig {
  private static final Logger log = LoggerFactory.getLogger(JobSchedulerBeanConfig.class);
  @Value("${spring.application.name}")
  private String applicationName;

  private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

  public JobSchedulerBeanConfig(ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
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
}
