package com.zzsong.job.common.loadbalancer.strategy;

import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 加权轮询策略
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class WeightRoundRobinLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {
    /**
     * <pre>
     *  后端目前的权重，一开始为 0 ~ weight 之间的随机数，之后会动态调整
     *  每次选取后端时，会遍历集群中所有后端，对于每个后端，让它的current_weight增加它的weight，
     *  同时累加所有后端的weight，保存为total。
     *  如果该后端的current_weight是最大的，就选定这个后端，然后把它的current_weight减去total。
     *  如果该后端没有被选定，那么current_weight不用减小。
     * </pre>
     */
    private final ConcurrentMap<String, AtomicInteger> defaultCurrentWeightMap
            = new ConcurrentHashMap<>();
    private final ConcurrentMap<Object, ConcurrentMap<String, AtomicInteger>> multiCurrentWeightMap
            = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public Server chooseServer(@Nullable Object key,
                               @Nonnull List<Server> servers) {
        if (servers.isEmpty()) {
            return null;
        }
        int size = servers.size();
        if (size == 1) {
            return servers.get(0);
        }

        ConcurrentMap<String, AtomicInteger> currentWeightMap = defaultCurrentWeightMap;
        if (key != null) {
            currentWeightMap = multiCurrentWeightMap
                    .computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());
        }
        int total = 0;
        Server selected = null;
        for (Server server : servers) {
            final String instanceId = server.getInstanceId();
            final int weight = server.getWeight();
            if (weight < 1) {
                throw new IllegalArgumentException("Weight least for 1");
            }
            total += weight;
            final AtomicInteger currentWeight = currentWeightMap
                    .computeIfAbsent(instanceId,
                            (k) -> new AtomicInteger(ThreadLocalRandom.current().nextInt(weight)));
            currentWeight.addAndGet(weight);
            if (selected == null) {
                selected = server;
            } else if (currentWeight.get() > currentWeightMap.get(selected.getInstanceId()).get()) {
                selected = server;
            }
        }
        final String selectedInstanceId = selected.getInstanceId();
        final AtomicInteger selectedCurrentWeight = currentWeightMap.get(selectedInstanceId);
        selectedCurrentWeight.addAndGet(-total);
        return selected;
    }
}
