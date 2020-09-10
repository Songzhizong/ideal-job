package com.zzsong.job.common.constants;

import com.zzsong.job.common.loadbalancer.LbStrategyEnum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/8/28
 */
public enum RouteStrategyEnum {
  /**
   * 忙碌转移
   */
  BUSY_TRANSFER("忙碌转移", LbStrategyEnum.BUSY_TRANSFER),
  /**
   * 一致性Hash
   */
  CONSISTENT_HASH("一致性Hash", LbStrategyEnum.CONSISTENT_HASH),
  /**
   * 最不经常使用
   */
  LFU("最不经常使用", LbStrategyEnum.LFU),
  /**
   * 最近最久未使用
   */
  LRU("最近最久未使用", LbStrategyEnum.LRU),
  /**
   * 轮询
   */
  ROUND_ROBIN("轮询", LbStrategyEnum.ROUND_ROBIN),
  /**
   * 随机
   */
  RANDOM("随机", LbStrategyEnum.RANDOM),
  /**
   * 加权轮询
   */
  WEIGHT_ROUND_ROBIN("加权轮询", LbStrategyEnum.WEIGHT_ROUND_ROBIN),
  /**
   * 加权随机
   */
  WEIGHT_RANDOM("加权随机", LbStrategyEnum.WEIGHT_RANDOM),
  /**
   * 广播
   */
  BROADCAST("广播", null),
  ;

  @Nonnull
  private final String desc;

  @Nullable
  private final LbStrategyEnum lbStrategy;

  RouteStrategyEnum(@Nonnull String desc,
                    @Nullable LbStrategyEnum lbStrategy) {
    this.desc = desc;
    this.lbStrategy = lbStrategy;
  }

  @Nonnull
  public String getDesc() {
    return desc;
  }

  @Nullable
  public LbStrategyEnum getLbStrategy() {
    return lbStrategy;
  }
}
