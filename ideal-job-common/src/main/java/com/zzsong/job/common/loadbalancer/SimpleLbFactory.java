package com.zzsong.job.common.loadbalancer;

import com.zzsong.job.common.loadbalancer.strategy.*;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗 on 2020/8/20
 */
public class SimpleLbFactory<Server extends LbServer> implements LbFactory<Server> {
  private final ConcurrentMap<String, LbServerHolder<Server>> serverHolderMap
      = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, LoadBalancer<Server>> loadBalancerMap
      = new ConcurrentHashMap<>();

  @Setter
  private LbStrategyEnum strategy = LbStrategyEnum.ROUND_ROBIN;
  private volatile boolean destroyed;
  @Nonnull
  private final List<LbFactoryEventListener<Server>> listeners = new ArrayList<>();


  @Nonnull
  @Override
  public List<Server> getReachableServers(@Nonnull String appName) {
    final LbServerHolder<Server> serverHolder = getServerHolder(appName);
    return serverHolder.getReachableServers();
  }

  @Nonnull
  @Override
  public Map<String, List<Server>> getReachableServers() {
    Map<String, List<Server>> map = new HashMap<>();
    serverHolderMap.forEach((appName, holder) -> map.put(appName, holder.getReachableServers()));
    return map;
  }

  @Override
  public void addServers(@Nonnull String appName, @Nonnull List<Server> newServers) {
    final LbServerHolder<Server> serverHolder = getServerHolder(appName);
    serverHolder.addServers(newServers);
  }

  @Override
  public void markServerDown(@Nonnull String appName, @Nonnull Server server) {
    final LbServerHolder<Server> serverHolder = getServerHolder(appName);
    serverHolder.markServerDown(server);
  }

  @Nullable
  @Override
  public Server chooseServer(@Nonnull String serverName,
                             @Nullable Object key,
                             @Nullable LbStrategyEnum strategy) {
    if (strategy == null) {
      strategy = this.strategy;
    }
    final LoadBalancer<Server> loadBalancer = getLoadBalancer(serverName, strategy);
    final LbServerHolder<Server> serverHolder = getServerHolder(serverName);
    return loadBalancer.chooseServer(key, serverHolder.getReachableServers());
  }

  @Nonnull
  @Override
  public LoadBalancer<Server> getLoadBalancer(@Nonnull String serverName,
                                              @Nullable LbStrategyEnum strategy) {
    LbStrategyEnum tmpStrategy;
    if (strategy == null) {
      tmpStrategy = this.strategy;
    } else {
      tmpStrategy = strategy;
    }

    String key = serverName + "-" + tmpStrategy.name();
    return loadBalancerMap.computeIfAbsent(key, (k) -> newLoadBalancer(tmpStrategy));
  }

  @Nonnull
  public LbServerHolder<Server> getServerHolder(@Nonnull String serverName) {
    return serverHolderMap
        .computeIfAbsent(serverName, k ->
            new SimpleLbServerHolder<>(serverName, this)
        );
  }

  @Override
  public void registerEventListener(@Nonnull LbFactoryEventListener<Server> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void serverChange(@Nonnull LbFactoryEvent event) {
    for (LbFactoryEventListener<Server> listener : listeners) {
      listener.onChange(this, event);
    }
  }

  @Override
  public void destroy() {
    if (!destroyed) {
      destroyed = true;
      Collection<LbServerHolder<Server>> serverHolders = serverHolderMap.values();
      for (LbServerHolder<Server> serverHolder : serverHolders) {
        serverHolder.destroy();
      }
    }
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  @Nonnull
  private LoadBalancer<Server> newLoadBalancer(@Nonnull LbStrategyEnum strategy) {
    switch (strategy) {
      case BUSY_TRANSFER: {
        return new BusyTransferLoadBalancer<>();
      }
      case CONSISTENT_HASH: {
        return new ConsistentHashLoadBalancer<>();
      }
      case FAIL_TRANSFER: {
        return new FailTransferLoadBalancer<>();
      }
      case LFU: {
        return new LFULoadBalancer<>();
      }
      case LRU: {
        return new LRULoadBalancer<>();
      }
      case ROUND_ROBIN: {
        return new RoundRobinLoadBalancer<>();
      }
      case RANDOM: {
        return new RandomLoadBalancer<>();
      }
      case WEIGHT_ROUND_ROBIN: {
        return new WeightRoundRobinLoadBalancer<>();
      }
      case WEIGHT_RANDOM: {
        return new WeightRandomLoadBalancer<>();
      }
      default: {
        return new RoundRobinLoadBalancer<>();
      }
    }
  }
}
