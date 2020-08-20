package cn.sh.ideal.nj.cmpt.job.common.loadbalancer.strategy;

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServer;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 故障转移
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class FailTransferLoadBalancer implements LoadBalancer {

  @Override
  @Nullable
  public LbServer chooseServer(@Nullable Object key, @Nonnull LbServerHolder serverHolder) {
    List<LbServer> reachableServers = serverHolder.getReachableServers();
    if (reachableServers.isEmpty()) {
      return null;
    }
    int size = reachableServers.size();
    if (size == 1) {
      return reachableServers.get(0);
    }
    for (LbServer reachableServer : reachableServers) {
      boolean available = reachableServer.heartbeat();
      if (available) {
        return reachableServer;
      }
    }
    return null;
  }
}
