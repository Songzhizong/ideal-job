package cn.sh.ideal.job.common.loadbalancer;

import cn.sh.ideal.job.common.loadbalancer.strategy.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SimpleLbFactory implements LbFactory {
  private final ConcurrentMap<String, LbServerHolder> serverHolderMap = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, LoadBalancer> loadBalancerMap = new ConcurrentHashMap<>();
  private volatile boolean destroyed;

  @Nullable
  @Override
  public LbServer chooseServer(@Nonnull String serverName,
                               @Nonnull LbStrategyEnum strategy,
                               @Nullable Object key) {
    final LoadBalancer loadBalancer = getLoadBalancer(serverName, strategy);
    final LbServerHolder serverHolder = getServerHolder(serverName);
    return loadBalancer.chooseServer(key, serverHolder);
  }

  @Override
  public LoadBalancer getLoadBalancer(@Nonnull String serverName,
                                      @Nonnull LbStrategyEnum strategy) {
    String key = serverName + "-" + strategy.getName();
    return loadBalancerMap.computeIfAbsent(key, (k) -> newLoadBalancer(strategy));
  }

  @Override
  public LbServerHolder getServerHolder(@Nonnull String serverName,
                                        @Nullable Function<String, LbServerHolder> function) {
    if (function == null) {
      return serverHolderMap.computeIfAbsent(serverName, (k) -> new SimpleServerHolder());
    } else {
      return serverHolderMap.computeIfAbsent(serverName, function);
    }
  }

  @Override
  public void destroy() {
    if (!destroyed) {
      destroyed = true;
      Collection<LbServerHolder> serverHolders = serverHolderMap.values();
      for (LbServerHolder serverHolder : serverHolders) {
        serverHolder.destroy();
      }
    }
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  private LoadBalancer newLoadBalancer(@Nonnull LbStrategyEnum strategy) {
    switch (strategy) {
      case BUSY_TRANSFER: {
        return new BusyTransferLoadBalancer();
      }
      case CONSISTENT_HASH: {
        return new ConsistentHashLoadBalancer();
      }
      case FAIL_TRANSFER: {
        return new FailTransferLoadBalancer();
      }
      case LFU: {
        return new LFULoadBalancer();
      }
      case LRU: {
        return new LRULoadBalancer();
      }
      case POLLING: {
        return new PollingLoadBalancer();
      }
      case RANDOM: {
        return new RandomLoadBalancer();
      }
      case WEIGHTED_POLLING: {
        return new WeightedPollingLoadBalancer();
      }
      case WEIGHTED_RANDOM: {
        return new WeightedRandomLoadBalancer();
      }
      default: {
        return new PollingLoadBalancer();
      }
    }
  }
}