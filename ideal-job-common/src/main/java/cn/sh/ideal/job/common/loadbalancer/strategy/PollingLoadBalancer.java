package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class PollingLoadBalancer implements LoadBalancer {
  private final AtomicInteger defaultCounter = new AtomicInteger(ThreadLocalRandom.current().nextInt(100));
  private final ConcurrentMap<Object, AtomicInteger> counterMap = new ConcurrentHashMap<>();

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
    AtomicInteger counter = defaultCounter;
    if (key != null) {
      counter = counterMap.computeIfAbsent(key,
          (k) -> new AtomicInteger(ThreadLocalRandom.current().nextInt(100)));
    }
    int abs = Math.abs(counter.incrementAndGet());
    return reachableServers.get(abs % size);
  }
}