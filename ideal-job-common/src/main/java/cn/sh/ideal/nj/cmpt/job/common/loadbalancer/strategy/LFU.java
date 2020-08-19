package cn.sh.ideal.nj.cmpt.job.common.loadbalancer.strategy;

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServer;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServerFactory;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 最不经常使用
 * <p>返回一定时间内使用次数最少的服务</p>
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class LFU implements LoadBalancer {
  /**
   * key 为空时使用
   * <p>
   * server instanceId -> server 最近选用次数
   */
  private ConcurrentMap<String, AtomicLong> defaultMap = new ConcurrentHashMap<>();
  /**
   * key 不为空时使用
   * <p>
   * key -> server instanceId -> server 最近选用次数
   */
  private ConcurrentMap<Object, ConcurrentMap<String, AtomicLong>> lfuMap = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();

  {
    scheduled.scheduleAtFixedRate(() -> {
      defaultMap = new ConcurrentHashMap<>();
      lfuMap = new ConcurrentHashMap<>();
    }, 8, 8, TimeUnit.HOURS);
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

    ConcurrentMap<String, AtomicLong> serverMap = defaultMap;
    if (key != null) {
      serverMap = lfuMap.computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());
    }

    String selectInstanceId = "";
    Long minCount = null;
    int bound = size * 10;
    for (LbServer server : reachableServers) {
      String instanceId = server.getInstanceId();
      AtomicLong atomicLong = serverMap.computeIfAbsent(instanceId,
          (k) -> new AtomicLong(ThreadLocalRandom.current().nextLong(bound)));
      long count = atomicLong.get();
      if (minCount == null || count < minCount) {
        minCount = count;
        selectInstanceId = instanceId;
      }
    }
    for (LbServer reachableServer : reachableServers) {
      if (selectInstanceId.equals(reachableServer.getInstanceId())) {
        serverMap.get(selectInstanceId).incrementAndGet();
        return reachableServer;
      }
    }
    return null;
  }
}
