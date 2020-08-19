package cn.sh.ideal.nj.cmpt.job.common.loadbalancer;

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
   * 可用性测试
   */
  boolean availableBeat();

  /**
   * 空闲度测试
   *
   * @param key 服务可以使用该对象针对处理
   * @return 空闲级别, 数字越小代表空闲程度越高, 小于1的值表示完全空闲
   */
  int idleBeat(@Nullable Object key);

  /**
   * @return 权重, 用于加权算法
   */
  default int weight() {
    return 1;
  }
}
