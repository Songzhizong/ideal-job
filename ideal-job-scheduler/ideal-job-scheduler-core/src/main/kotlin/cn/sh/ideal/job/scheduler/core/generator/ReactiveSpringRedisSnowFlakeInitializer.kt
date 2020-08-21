package cn.sh.ideal.job.scheduler.core.generator

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * 基于redis的SnowFlake初始化器
 *
 * @author 宋志宗
 * @date 2020/5/23
 */
@Suppress("unused")
class ReactiveSpringRedisSnowFlakeInitializer(
    /**
     * 注册到redis的machineId过期时间,秒
     */
    expireSeconds: Long,
    /**
     * 注册到redis的machineId自动续期间隔,秒
     */
    private val renewalIntervalSeconds: Long,
    /**
     * 服务名称
     */
    private val applicationName: String,
    /**
     * [ReactiveStringRedisTemplate]
     */
    private val stringRedisTemplate: ReactiveStringRedisTemplate) : SnowFlakeInitializer {
  private val log: Logger = LoggerFactory.getLogger(this.javaClass)

  private val prefix = "ideal:register:snowflake:$applicationName:machineId"
  private val expire: Duration = Duration.ofSeconds(expireSeconds)
  private var machineId = -1L
  private var automaticallyRenewal = false
  private var executorService: ScheduledExecutorService? = null

  override fun init() {
    if (SnowFlake.dataCenterId < 0) {
      SnowFlake.dataCenterId = 0
    }
    if (SnowFlake.machineId > -1L) {
      return
    }
    val operations = stringRedisTemplate.opsForValue()
    while (true) {
      ++machineId
      val success = operations
          .setIfAbsent(prefix + machineId, "machineId", expire).block()
          ?: false
      if (success) {
        SnowFlake.machineId = machineId
        break
      }
      val maxMachineNum = SnowFlake.maxMachineNum
      if (machineId >= maxMachineNum) {
        log.error("停止服务, SnowFlake machineId 计算失败,已达上限:{}", maxMachineNum)
        exitProcess(0)
      }
    }
    // 开始自动续期
    automaticallyRenewed()
    Runtime.getRuntime().addShutdownHook(Thread {
      // 先把自动续期停掉
      executorService?.shutdown()
      // 释放机器码
      operations.delete(prefix + machineId).block()
      log.info("release SnowFlake machineId: {} -> {}", applicationName, machineId)
    })
    log.info("SnowFlake dataCenterId = {}, machineId = {}", SnowFlake.dataCenterId, SnowFlake.machineId)
    return
  }

  /**
   * 自动续期
   * 每隔一段时间对机器码续期
   */
  private fun automaticallyRenewed() {
    if (automaticallyRenewal) {
      return
    }
    if (executorService == null) {
      executorService = Executors.newSingleThreadScheduledExecutor()
    }
    executorService!!.scheduleAtFixedRate({
      val key = prefix + machineId
      log.debug("SnowFlake automatically renewed: {} ...", key)
      stringRedisTemplate.expire(key, expire)
          .doOnError {
            log.warn("SnowFlake automatically renewed exception: {}", it.message)
          }.subscribe()
    }, renewalIntervalSeconds, renewalIntervalSeconds, TimeUnit.SECONDS)
    automaticallyRenewal = true
  }
}