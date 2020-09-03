package com.zzsong.job.common.loadbalancer;

import com.zzsong.job.common.loadbalancer.strategy.*;

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
public class SimpleLbFactory<Server extends LbServer> implements LbFactory<Server> {
    private final ConcurrentMap<String, LbServerHolder<Server>> serverHolderMap
            = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LoadBalancer<Server>> loadBalancerMap
            = new ConcurrentHashMap<>();
    private volatile boolean destroyed;

    @Nullable
    @Override
    public Server chooseServer(@Nonnull String serverName,
                               @Nonnull LbStrategyEnum strategy,
                               @Nullable Object key) {
        final LoadBalancer<Server> loadBalancer = getLoadBalancer(serverName, strategy);
        final LbServerHolder<Server> serverHolder = getServerHolder(serverName, null);
        return loadBalancer.chooseServer(key, serverHolder.getReachableServers());
    }

    @Nonnull
    @Override
    public LoadBalancer<Server> getLoadBalancer(@Nonnull String serverName,
                                                @Nonnull LbStrategyEnum strategy) {
        String key = serverName + "-" + strategy.name();
        return loadBalancerMap.computeIfAbsent(key, (k) -> newLoadBalancer(strategy));
    }

    @Nonnull
    @Override
    public LbServerHolder<Server> getServerHolder(
            @Nonnull String serverName,
            @Nullable Function<String, LbServerHolder<Server>> function) {
        LbServerHolder<Server> serverHolder;
        if (function == null) {
            serverHolder = serverHolderMap.computeIfAbsent(serverName, (k) -> new SimpleLbServerHolder<>(serverName));
        } else {
            serverHolder = serverHolderMap.computeIfAbsent(serverName, function);
        }
        return serverHolder;
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
