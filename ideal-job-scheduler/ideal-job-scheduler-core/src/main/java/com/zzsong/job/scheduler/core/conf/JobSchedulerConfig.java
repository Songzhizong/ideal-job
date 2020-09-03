package com.zzsong.job.scheduler.core.conf;

import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.loadbalancer.SimpleLbFactory;
import com.zzsong.job.common.utils.IpUtil;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.scheduler.core.generator.IDGenerator;
import com.zzsong.job.scheduler.core.generator.ReactiveRedisRegisterSnowFlake;
import com.zzsong.job.scheduler.core.dispatch.TimingSchedule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.*;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Configuration
public class JobSchedulerConfig {
    private static final Logger log = LoggerFactory.getLogger(JobSchedulerConfig.class);
    @Value("${spring.application.name}")
    private String applicationName;

    private String ip = null;
    private int port = -1;
    private String ipPort = null;
    private final WebServerApplicationContext context;
    private final JobSchedulerProperties schedulerProperties;

    public JobSchedulerConfig(WebServerApplicationContext context,
                              JobSchedulerProperties schedulerProperties) {
        this.context = context;
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
        if (port > 1) {
            return port;
        }
        port = context.getWebServer().getPort();
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

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public LbFactory<TaskWorker> lbFactory() {
        return new SimpleLbFactory<>();
    }

    @Bean
    public IDGenerator idGenerator(ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        ReactiveRedisRegisterSnowFlake snowFlake = new ReactiveRedisRegisterSnowFlake(
                0, 300, 60,
                applicationName, reactiveStringRedisTemplate);
        log.debug("test SnowFlake generate: " + snowFlake.generate());
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

    @Bean
    public ExecutorService cronJobThreadPool() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolProperties properties = schedulerProperties.getCronJobTriggerPool();
        int corePoolSize = properties.getCorePoolSize();
        if (corePoolSize < 0) {
            corePoolSize = processors << 3;
        }
        int maximumPoolSize = properties.getMaximumPoolSize();
        if (maximumPoolSize < 1) {
            maximumPoolSize = processors << 5;
        }
        BlockingQueue<Runnable> workQueue;
        int workQueueSize = properties.getWorkQueueSize();
        if (workQueueSize < 1) {
            workQueue = new SynchronousQueue<>();
        } else {
            workQueue = new ArrayBlockingQueue<>(workQueueSize);
        }
        int finalCorePoolSize = corePoolSize;
        int finalMaximumPoolSize = maximumPoolSize;
        final ThreadPoolExecutor poolExecutor
                = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                60, TimeUnit.SECONDS, workQueue,
                new ThreadFactoryBuilder().setNameFormat("cron-job-pool-%d").build(),
                (runnable, executor) -> {
                    if (!executor.isShutdown()) {
                        final Logger log = TimingSchedule.log;
                        log.error("cron-job-pool 无法接受新任务 已在调用线程执行, corePoolSize={}, maximumPoolSize={}, workQueueSize={}",
                                finalCorePoolSize, finalMaximumPoolSize, workQueueSize);
                        runnable.run();
                    }
                });
        poolExecutor.allowCoreThreadTimeOut(true);
        return poolExecutor;
    }

    @Bean
    public CacheManager cacheManager(@Nonnull RedisConnectionFactory redisConnectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter
                .nonLockingRedisCacheWriter(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Object> serializer
                = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                .activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL)
                .registerModule(JsonUtils.getJavaTimeModule())
                .findAndRegisterModules();
        serializer.setObjectMapper(objectMapper);
        RedisCacheConfiguration redisCacheConfiguration
                = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofHours(36));
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }
}
