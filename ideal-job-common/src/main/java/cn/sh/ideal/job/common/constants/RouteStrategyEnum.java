package cn.sh.ideal.job.common.constants;

import cn.sh.ideal.job.common.loadbalancer.LbStrategyEnum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
public enum RouteStrategyEnum {
  /**
   * 忙碌转移
   */
  BUSY_TRANSFER(1, "忙碌转移", LbStrategyEnum.BUSY_TRANSFER),
  /**
   * 一致性Hash
   */
  CONSISTENT_HASH(2, "一致性Hash", LbStrategyEnum.CONSISTENT_HASH),
  /**
   * 故障转移
   */
  FAIL_TRANSFER(3, "故障转移", LbStrategyEnum.FAIL_TRANSFER),
  /**
   * 最不经常使用
   */
  LFU(4, "最不经常使用", LbStrategyEnum.LFU),
  /**
   * 最近最久未使用
   */
  LRU(5, "最近最久未使用", LbStrategyEnum.LRU),
  /**
   * 轮询
   */
  POLLING(6, "轮询", LbStrategyEnum.POLLING),
  /**
   * 随机
   */
  RANDOM(7, "随机", LbStrategyEnum.RANDOM),
  /**
   * 加权轮询
   */
  WEIGHTED_POLLING(8, "加权轮询", LbStrategyEnum.WEIGHTED_POLLING),
  /**
   * 加权随机
   */
  WEIGHTED_RANDOM(9, "加权随机", LbStrategyEnum.WEIGHTED_RANDOM),
  ;

  private final int code;

  @Nonnull
  private final String name;

  @Nullable
  private final LbStrategyEnum lbStrategy;

  RouteStrategyEnum(int code, @Nonnull String name, @Nullable LbStrategyEnum lbStrategy) {
    this.code = code;
    this.name = name;
    this.lbStrategy = lbStrategy;
  }

  public int getCode() {
    return code;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nullable
  public LbStrategyEnum getLbStrategy() {
    return lbStrategy;
  }
}
