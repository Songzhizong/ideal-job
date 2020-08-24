package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 最不经常使用
 * <p>返回一定时间内使用次数最少的服务</p>
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class LFULoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {
  /**
   * key 为空时使用
   * <p>
   * server instanceId -> server 最近选用次数
   */
  private ConcurrentMap<String, AtomicLong> defaultLfuMap = new ConcurrentHashMap<>();
  /**
   * key 不为空时使用
   * <p>
   * key -> server instanceId -> server 最近选用次数
   */
  private ConcurrentMap<Object, ConcurrentMap<String, AtomicLong>> multiLfuMap
      = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduled
      = Executors.newSingleThreadScheduledExecutor();

  {
    scheduled.scheduleAtFixedRate(() -> {
      defaultLfuMap = new ConcurrentHashMap<>();
      multiLfuMap = new ConcurrentHashMap<>();
    }, 8, 8, TimeUnit.HOURS);
  }

  @Override
  @Nullable
  public Server chooseServer(@Nullable Object key,
                             @Nonnull LbServerHolder<Server> serverHolder) {
    List<Server> reachableServers = serverHolder.getReachableServers();
    if (reachableServers.isEmpty()) {
      return null;
    }
    int size = reachableServers.size();
    if (size == 1) {
      return reachableServers.get(0);
    }

    ConcurrentMap<String, AtomicLong> lfuMap = defaultLfuMap;
    if (key != null) {
      lfuMap = multiLfuMap.computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());
    }

    Server selected = null;
    Long minCount = null;
    int bound = size * 10;
    for (Server server : reachableServers) {
      String instanceId = server.getInstanceId();
      AtomicLong atomicLong = lfuMap.computeIfAbsent(instanceId,
          (k) -> new AtomicLong(ThreadLocalRandom.current().nextLong(bound)));
      long count = atomicLong.get();
      if (minCount == null || count < minCount) {
        minCount = count;
        selected = server;
      }
    }
    return selected;
  }
}
