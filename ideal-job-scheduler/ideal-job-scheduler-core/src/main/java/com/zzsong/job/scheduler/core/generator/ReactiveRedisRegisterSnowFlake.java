package com.zzsong.job.scheduler.core.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通过Redis注册机器码的SnowFlake实现
 *
 * @author 宋志宗 on 2020/9/2
 */
public class ReactiveRedisRegisterSnowFlake implements IDGenerator {
  private static final Logger log = LoggerFactory
      .getLogger(ReactiveRedisRegisterSnowFlake.class);
  private final String prefix;
  private final Duration expire;
  private final long expireSeconds;
  private final long renewalIntervalSeconds;
  @Nonnull
  private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

  private long machineId = -1;
  private final SnowFlake snowFlake;
  private boolean automaticallyRenewal = false;
  private ScheduledExecutorService executorService;

  public ReactiveRedisRegisterSnowFlake(
      @Nonnull String applicationName,
      @Nonnull ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    this(0, applicationName, reactiveStringRedisTemplate);
  }

  public ReactiveRedisRegisterSnowFlake(
      long dataCenterId,
      @Nonnull String applicationName,
      @Nonnull ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    this(dataCenterId, 300, 60,
        applicationName, reactiveStringRedisTemplate);
  }

  public ReactiveRedisRegisterSnowFlake(
      long dataCenterId,
      long expireSeconds,
      long renewalIntervalSeconds,
      @Nonnull String applicationName,
      @Nonnull ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    int maxDataCenterNum = SnowFlake.MAX_DATA_CENTER_NUM;
    if (dataCenterId < 0 || dataCenterId > maxDataCenterNum) {
      log.warn("dataCenterId must >=0 and <=" + maxDataCenterNum);
      dataCenterId = 0;
    }
    this.prefix = "ideal:register:snowflake:machineId:" + applicationName + ":";
    this.expire = Duration.ofSeconds(expireSeconds);
    this.expireSeconds = expireSeconds;
    this.renewalIntervalSeconds = renewalIntervalSeconds;
    this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    int maxMachineNum = SnowFlake.MAX_MACHINE_NUM;
    ReactiveValueOperations<String, String> operations
        = reactiveStringRedisTemplate.opsForValue();
    while (true) {
      ++machineId;
      Boolean success = operations
          .setIfAbsent(prefix + machineId, "1", expire).block();
      if (success != null && success) {
        log.info("SnowFlake register success: applicationName = {}, machineId = {}",
            applicationName, machineId);
        this.snowFlake = new SnowFlake(dataCenterId, machineId);
        break;
      }
      if (machineId >= maxMachineNum) {
        log.error("停止服务, SnowFlake machineId 计算失败,已达上限:{}",
            maxMachineNum);
        System.exit(0);
      }
    }
    // 开始自动续期
    automaticallyRenewed();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // 先把自动续期停掉
      if (executorService != null) {
        executorService.shutdownNow();
      }
      // 释放机器码
      operations.delete(prefix + machineId).block();
    }));
    log.info("SnowFlake dataCenterId = {}, machineId = {}",
        dataCenterId, machineId);
  }

  private void automaticallyRenewed() {
    if (automaticallyRenewal) {
      return;
    }
    log.info("SnowFlake start automatically renewed, renewed cycle = {}s, expire = {}s",
        renewalIntervalSeconds, expireSeconds);
    if (executorService == null) {
      executorService = Executors.newSingleThreadScheduledExecutor();
    }
    executorService.scheduleAtFixedRate(() ->
            reactiveStringRedisTemplate
                .expire(prefix + machineId, expire)
                .doOnError(throwable -> {
                  String errMsg = throwable.getClass().getName() +
                      ": " + throwable.getMessage();
                  log.error("SnowFlake automatically renewed exception: {}",
                      errMsg);
                }).subscribe(),
        renewalIntervalSeconds, renewalIntervalSeconds, TimeUnit.SECONDS);
    automaticallyRenewal = true;
  }

  @Override
  public long generate() {
    return snowFlake.generate();
  }
}
