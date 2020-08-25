package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 一致性Hash策略
 * <p>See <a href="https://github.com/xuxueli/xxl-job/blob/master/xxl-job-admin/src/main/java/com/xxl/job/admin/core/route/strategy/ExecutorRouteConsistentHash.java">xxl-job ExecutorRouteConsistentHash</a></p>
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class ConsistentHashLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {


  private static long hash(@Nonnull Object key) {
    // md5 byte
    MessageDigest md5;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 not supported", e);
    }
    md5.reset();
    byte[] keyBytes = key.toString().getBytes(StandardCharsets.UTF_8);
    md5.update(keyBytes);
    byte[] digest = md5.digest();
    // hash code, Truncate to 32-bits
    long hashCode = ((long) (digest[3] & 0xFF) << 24)
        | ((long) (digest[2] & 0xFF) << 16)
        | ((long) (digest[1] & 0xFF) << 8)
        | (digest[0] & 0xFF);
    return hashCode & 0xffffffffL;
  }

  /**
   * 如果key为null, 则采用随机算法
   *
   * @param key          负载均衡器可以使用该对象来确定返回哪个服务
   * @param reachableServers 可达服务列表
   * @return LbServer
   */
  @Override
  @Nullable
  public Server chooseServer(@Nullable Object key,
                             @Nonnull List<Server> reachableServers) {
    final int virtualNodeNum = 100;
    if (reachableServers.isEmpty()) {
      return null;
    }
    int size = reachableServers.size();
    if (size == 1) {
      return reachableServers.get(0);
    }
    if (key == null) {
      int random = ThreadLocalRandom.current().nextInt(size);
      return reachableServers.get(random);
    }
    TreeMap<Long, Server> addressRing = new TreeMap<>();
    for (Server server : reachableServers) {
      final String instanceId = server.getInstanceId();
      for (int i = 0; i < virtualNodeNum; i++) {
        long addressHash = hash("SHARD-" + instanceId + "-NODE-" + i);
        addressRing.put(addressHash, server);
      }
    }
    long keyHash = hash(String.valueOf(key));
    SortedMap<Long, Server> lastRing = addressRing.tailMap(keyHash);
    if (lastRing.isEmpty()) {
      return addressRing.firstEntry().getValue();
    }
    return lastRing.get(lastRing.firstKey());
  }
}
