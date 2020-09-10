package com.zzsong.job.common.loadbalancer;

import javax.annotation.Nonnull;

/**
 * 负载均衡策略枚举
 *
 * @author 宋志宗 on 2020/8/20
 */
public enum LbStrategyEnum {
  /**
   * 忙碌转移
   */
  BUSY_TRANSFER("忙碌转移"),
  /**
   * 一致性Hash
   */
  CONSISTENT_HASH("一致性Hash"),
  /**
   * 故障转移
   */
  FAIL_TRANSFER("故障转移"),
  /**
   * 最不经常使用
   */
  LFU("最不经常使用"),
  /**
   * 最近最久未使用
   */
  LRU("最近最久未使用"),
  /**
   * 轮询
   */
  ROUND_ROBIN("轮询"),
  /**
   * 随机
   */
  RANDOM("随机"),
  /**
   * 加权轮询
   */
  WEIGHT_ROUND_ROBIN("加权轮询"),
  /**
   * 加权随机
   */
  WEIGHT_RANDOM("加权随机"),
  ;

  @Nonnull
  private final String desc;

  LbStrategyEnum(@Nonnull String desc) {
    this.desc = desc;
  }

  @Nonnull
  public String getDesc() {
    return desc;
  }
}
