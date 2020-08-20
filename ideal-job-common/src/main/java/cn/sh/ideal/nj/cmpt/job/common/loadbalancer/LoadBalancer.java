package cn.sh.ideal.nj.cmpt.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/19
 */
public interface LoadBalancer {
  /**
   * 选取一个server
   *
   * @param key     负载均衡器可以使用该对象来确定返回哪个服务
   * @param serverHolder 服务工厂, 存储了所有的服务对象
   */
  @Nullable
  LbServer chooseServer(@Nullable Object key, @Nonnull LbServerHolder serverHolder);
}
