package com.zzsong.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/19
 */
public interface LbServer {

  /**
   * @return 唯一ID
   */
  @Nonnull
  String getInstanceId();

  /**
   * 心跳检测
   * <pre>
   *   1. LbServerFactory心跳测试
   *   2. 故障转移
   * </pre>
   */
  boolean heartbeat();

  /**
   * 空闲度测试
   * <p>如果需要使用忙碌转移策略, 请实现该方法</p>
   *
   * @param key 服务可以使用该对象针对处理
   * @return 空闲级别, 数字越小代表空闲程度越高, 小于1的值表示完全空闲
   */
  default int idleBeat(@Nullable Object key) {
    final String className = this.getClass().getName();
    throw new NotImplementedException(className + " not implemented idleBeat");
  }

  /**
   * 获取权重
   * <p>如果需要使用加权策略, 请实现该方法</p>
   *
   * @return 权重, 用于加权算法, 至少为1
   */
  default int getWeight() {
    final String className = this.getClass().getName();
    throw new NotImplementedException(className + " not implemented getWeight");
  }

  /**
   * 销毁对象
   */
  default void destroy() {
    // non
  }

//  @Override
//  default int compareTo(@Nonnull LbServer o) {
//    return this.getWeight() - o.getWeight();
//  }
}
