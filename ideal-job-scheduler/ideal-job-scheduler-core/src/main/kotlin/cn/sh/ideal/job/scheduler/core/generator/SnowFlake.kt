package cn.sh.ideal.job.scheduler.core.generator

import org.apache.commons.lang3.StringUtils
import java.lang.UnsupportedOperationException
import java.util.concurrent.ConcurrentHashMap

/**
 * 雪花ID生成器
 *
 * @author 宋志宗
 * @date 2019/9/3
 */
@Suppress("unused")
object SnowFlake : IDGenerator {
  /** systemProperty中保存数据中心ID的key */
  private const val systemPropertyKeyDataCenterId = "dataCenterId"

  /** systemProperty中保存机器ID的key */
  private const val systemPropertyKeyMachineId = "machineId"

  /** 默认业务码 */
  private const val defaultBiz = "default"

  /** 雪花算法实例映射  业务码 -> 对象实例 */
  private val instanceMap = ConcurrentHashMap<String, SnowFlakeInst>()

  /** 数据中心id */
  @Suppress("MemberVisibilityCanBePrivate")
  var dataCenterId = -1L
    set(value) {
      if (dataCenterId > -1) {
        throw SnowFlakeInitializeError("dataCenterId已被初始化")
      }
      field = value
    }

  /** 机器id */
  @Suppress("MemberVisibilityCanBePrivate")
  var machineId = -1L
    set(value) {
      if (machineId > -1) {
        throw SnowFlakeInitializeError("machineId已被初始化")
      }
      field = value
    }

  /** 起始时间戳 */
  private const val startTimestamp = 1567475197889L

  /** 序列号位数 */
  private const val sequenceBit = 12

  /** 机器码占用的位数 */
  private const val machineBit = 6

  /** 数据中心占用的位数 */
  private const val dataCenterBit = 4

  /** 序列号最大值 */
  private const val maxSequenceNum = 1L.shl(sequenceBit) - 1

  /** 机器码最大值 */
  internal const val maxMachineNum = 1L.shl(machineBit) - 1

  /** 数据中心最大值 */
  private const val maxDataCenterNum = 1L.shl(dataCenterBit) - 1

  /** 机器码向左的位移 */
  private const val machineLeft = sequenceBit

  /** 数据中心向左的位移 */
  private const val dataCenterLeft = sequenceBit + machineBit

  /** 时间戳向左的位移 */
  private const val timestampLeft = dataCenterLeft + dataCenterBit


  init {
    val dataCenterId = System.getProperty(systemPropertyKeyDataCenterId)
    val machineId = System.getProperty(systemPropertyKeyMachineId)
    if (StringUtils.isNotBlank(dataCenterId)) {
      SnowFlake.dataCenterId = try {
        dataCenterId.toLong().also { require(it <= maxDataCenterNum) { "数据中心编码过大" } }
      } catch (e: Exception) {
        -1L
      }
    }
    if (StringUtils.isNotBlank(machineId)) {
      SnowFlake.machineId = try {
        machineId.toLong().also { require(it <= maxMachineNum) { "机器码过大" } }
      } catch (e: Exception) {
        -1L
      }
    }
  }

  /**
   * 获取分布式ID
   *
   * @param biz 业务码
   */
  private fun doGenerate(biz: String = defaultBiz): Long {
    if (dataCenterId < 0 || machineId < 0) {
      throw SnowFlakeInitializeError("未正确设置数据中心和机器编码")
    }
    return getBizGenerator(biz).generate()
  }

  override fun generate(): Long {
    return doGenerate()
  }

  override fun generate(biz: String): Long {
    return doGenerate(biz)
  }

  private fun getBizGenerator(biz: String): IDGenerator {
    return instanceMap.computeIfAbsent(biz) { SnowFlakeInst(dataCenterId, machineId) }
  }

  internal class SnowFlakeInst(
      /** 数据中心id */
      private val dataCenterId: Long,
      /** 机器id */
      private val machineId: Long) : IDGenerator {

    private var sequence = 0L
    private var lasTimestamp = -1L

    override fun generate(biz: String): Long {
      throw UnsupportedOperationException()
    }

    override fun generate(): Long {
      return synchronized(this) {
        var currTimestamp = System.currentTimeMillis()
        if (currTimestamp < lasTimestamp) {
          throw  RuntimeException("Clock moved backwards.  Refusing to generate id")
        }
        if (currTimestamp == lasTimestamp) {
          sequence = (sequence + 1).and(maxSequenceNum)
          if (sequence == 0L) {
            currTimestamp = getNextMill()
          }
        } else {
          sequence = 0L
        }
        lasTimestamp = currTimestamp
        ((currTimestamp - startTimestamp).shl(timestampLeft))
            .or(this.dataCenterId.shl(dataCenterLeft))
            .or(this.machineId.shl(machineLeft))
            .or(sequence)
      }
    }

    private fun getNextMill(): Long {
      var mill = System.currentTimeMillis()
      while (mill <= lasTimestamp) {
        mill = System.currentTimeMillis()
      }
      return mill
    }
  }

  class SnowFlakeInitializeError(msg: String) : Error(msg)
}