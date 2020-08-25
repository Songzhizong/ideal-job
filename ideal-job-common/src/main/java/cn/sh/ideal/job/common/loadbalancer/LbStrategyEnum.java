package cn.sh.ideal.job.common.loadbalancer;

import javax.annotation.Nonnull;

/**
 * 负载均衡策略枚举
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public enum LbStrategyEnum {
  /**
   * 忙碌转移
   */
  BUSY_TRANSFER(1, "忙碌转移"),
  /**
   * 一致性Hash
   */
  CONSISTENT_HASH(2, "一致性Hash"),
  /**
   * 故障转移
   */
  FAIL_TRANSFER(3, "故障转移"),
  /**
   * 最不经常使用
   */
  LFU(4, "最不经常使用"),
  /**
   * 最近最久未使用
   */
  LRU(5, "最近最久未使用"),
  /**
   * 轮询
   */
  POLLING(6, "轮询"),
  /**
   * 随机
   */
  RANDOM(7, "随机"),
  /**
   * 加权轮询
   */
  WEIGHTED_POLLING(8, "加权轮询"),
  /**
   * 加权随机
   */
  WEIGHTED_RANDOM(9, "加权随机"),
  ;

  private final int code;

  @Nonnull
  private final String name;

  LbStrategyEnum(int code, @Nonnull String name) {
    this.code = code;
    this.name = name;
  }

  public int getCode() {
    return code;
  }

  @Nonnull
  public String getName() {
    return name;
  }
}
