package cn.sh.ideal.nj.cmpt.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 负载均衡器工厂
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface LbFactory {

  /**
   * 选取一个server, 默认为轮询策略
   *
   * @param serverName 服务名称
   * @param key        负载均衡器可以使用该对象来确定返回哪个服务
   * @author 宋志宗
   * @date 2020/8/20 9:59 上午
   */
  @Nullable
  default LbServer chooseServer(@Nonnull String serverName,
                                @Nullable Object key) {
    return chooseServer(serverName, LbStrategyEnum.POLLING, key);
  }

  /**
   * 选取一个server
   *
   * @param serverName 服务名称
   * @param strategy   负载均衡策略
   * @param key        负载均衡器可以使用该对象来确定返回哪个服务
   * @author 宋志宗
   * @date 2020/8/20 9:59 上午
   */
  @Nullable
  LbServer chooseServer(@Nonnull String serverName,
                        @Nonnull LbStrategyEnum strategy,
                        @Nullable Object key);

  /**
   * 获取负载均衡器, 默认为轮询策略
   *
   * @param serverName 服务名称
   * @return LoadBalancer
   * @author 宋志宗
   * @date 2020/8/20 9:50 上午
   */
  default LoadBalancer getLoadBalancer(@Nonnull String serverName) {
    return getLoadBalancer(serverName, LbStrategyEnum.POLLING);
  }

  /**
   * 获取负载均衡器
   *
   * @param serverName 服务名称
   * @param strategy   负载均衡策略
   * @return LoadBalancer
   * @author 宋志宗
   * @date 2020/8/20 9:50 上午
   */
  LoadBalancer getLoadBalancer(@Nonnull String serverName,
                               @Nonnull LbStrategyEnum strategy);

  /**
   * 获取服务持有者
   *
   * @param serverName 服务名称
   * @return LbServerFactory
   * @author 宋志宗
   * @date 2020/8/20 10:27 上午
   */
  LbServerHolder getServerHolder(@Nonnull String serverName);
}
