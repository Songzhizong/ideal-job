package cn.sh.ideal.nj.cmpt.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/19
 */
public interface LbServer extends Comparable<LbServer> {

  /**
   * @return 唯一ID
   */
  @Nonnull
  String getInstanceId();

  /**
   * 可用性测试
   * <pre>
   *   1. LbServerFactory心跳测试
   *   2. 故障转移
   * </pre>
   */
  boolean availableBeat();

  /**
   * 空闲度测试
   * <p>忙碌转移策略需要调用该方法</p>
   *
   * @param key 服务可以使用该对象针对处理
   * @return 空闲级别, 数字越小代表空闲程度越高, 小于1的值表示完全空闲
   */
  int idleBeat(@Nullable Object key);

  /**
   * @return 权重, 用于加权算法, 至少为1
   */
  default int getWeight() {
    return 1;
  }

  /**
   * @return 权重, 用于加权算法, 至少为1
   */
  default int checkAndGetWeight() {
    final int weight = getWeight();
    if (weight < 1) {
      throw new IllegalArgumentException("weight least for 1");
    }
    return weight;
  }

  @Override
  default int compareTo(@Nonnull LbServer o) {
    return this.getWeight() - o.getWeight();
  }
}
