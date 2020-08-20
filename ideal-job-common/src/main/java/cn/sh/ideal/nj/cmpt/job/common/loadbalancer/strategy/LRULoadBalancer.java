package cn.sh.ideal.nj.cmpt.job.common.loadbalancer.strategy;

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServer;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 最近最久未使用
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class LRULoadBalancer implements LoadBalancer {

  private final ConcurrentMap<String, Long> defaultLruMap = new ConcurrentHashMap<>();
  private final ConcurrentMap<Object, ConcurrentMap<String, Long>> multiLruMap
      = new ConcurrentHashMap<>();

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

    final long currentTimeMillis = System.currentTimeMillis();
    LbServer selected = null;
    Long maxDifference = null;
    ConcurrentMap<String, Long> lruMap = defaultLruMap;
    if (key != null) {
      multiLruMap.computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());
    }
    for (LbServer server : reachableServers) {
      final String instanceId = server.getInstanceId();
      final Long lastSelectTime = lruMap.putIfAbsent(instanceId, 0L);
      assert lastSelectTime != null;
      final long difference = currentTimeMillis - lastSelectTime;
      if (maxDifference == null || difference > maxDifference) {
        maxDifference = difference;
        selected = server;
      }
    }
    if (selected != null) {
      lruMap.put(selected.getInstanceId(), currentTimeMillis);
    }
    return selected;
  }
}
