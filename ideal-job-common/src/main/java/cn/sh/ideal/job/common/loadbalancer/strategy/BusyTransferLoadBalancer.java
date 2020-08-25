package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 忙碌转移
 * <pre>
 *   选择一个空闲度最高的服务返回.
 *   如果{@link BusyTransferLoadBalancer#completelyIdle}为true则选择一个完全空闲的服务,
 *   没有完全空闲的服务则返回null
 * </pre>
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class BusyTransferLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {
  /**
   * 是否需要选择完全空闲的服务
   */
  private boolean completelyIdle = false;

  public BusyTransferLoadBalancer() {
  }

  /**
   * @param completelyIdle 是否需要选择完全空闲的服务
   */
  public BusyTransferLoadBalancer(boolean completelyIdle) {
    this.completelyIdle = completelyIdle;
  }

  @Override
  @Nullable
  public Server chooseServer(@Nullable Object key,
                             @Nonnull List<Server> reachableServers) {
    if (reachableServers.isEmpty()) {
      return null;
    }
    int size = reachableServers.size();
    if (size == 1) {
      return reachableServers.get(0);
    }

    Server selected = null;
    Integer minIdleLevel = null;
    for (Server server : reachableServers) {
      int idleLevel = server.idleBeat(key);
      if (idleLevel < 1) {
        return server;
      }
      if (minIdleLevel == null || idleLevel < minIdleLevel) {
        minIdleLevel = idleLevel;
        selected = server;
      }
    }
    if (!completelyIdle) {
      return selected;
    }
    return null;
  }
}
