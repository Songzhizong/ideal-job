package cn.sh.ideal.nj.cmpt.job.common.loadbalancer.strategy;

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServer;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServerFactory;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 忙碌转移
 * <pre>
 *   选择一个空闲度最高的服务返回.
 *   如果{@link BusyTransfer#completelyIdle}为true则选择一个完全空闲的服务,
 *   没有完全空闲的服务则返回null
 * </pre>
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class BusyTransfer implements LoadBalancer {
  /**
   * 是否需要选择完全空闲的服务
   */
  private boolean completelyIdle = false;

  public BusyTransfer() {
  }

  /**
   * @param completelyIdle 是否需要选择完全空闲的服务
   */
  public BusyTransfer(boolean completelyIdle) {
    this.completelyIdle = completelyIdle;
  }

  @Override
  @Nullable
  public LbServer chooseServer(@Nullable Object key, @Nonnull LbServerFactory factory) {
    List<LbServer> reachableServers = factory.getReachableServers();
    if (reachableServers.isEmpty()) {
      return null;
    }
    int size = reachableServers.size();
    if (size == 1) {
      return reachableServers.get(0);
    }

    String idleInstanceId = "";
    Integer minIdleLevel = null;
    for (LbServer reachableServer : reachableServers) {
      int idleLevel = reachableServer.idleBeat(key);
      if (idleLevel < 1) {
        return reachableServer;
      }
      if (minIdleLevel == null || idleLevel < minIdleLevel) {
        minIdleLevel = idleLevel;
        idleInstanceId = reachableServer.getInstanceId();
      }
    }
    if (!completelyIdle) {
      for (LbServer reachableServer : reachableServers) {
        if (idleInstanceId.equals(reachableServer.getInstanceId())) {
          return reachableServer;
        }
      }
    }
    return null;
  }
}
