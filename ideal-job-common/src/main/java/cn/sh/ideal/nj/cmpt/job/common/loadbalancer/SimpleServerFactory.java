package cn.sh.ideal.nj.cmpt.job.common.loadbalancer;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SimpleServerFactory implements LbServerFactory {

  private final List<LbServer> allServers = new ArrayList<>();
  private final ConcurrentMap<String, LbServer> reachableServerMap = new ConcurrentHashMap<>();

  @Override
  public void addServers(@Nonnull List<LbServer> newServers) {
    synchronized (this) {
      Set<String> existIds = allServers.stream()
          .map(LbServer::getInstanceId).collect(Collectors.toSet());
      for (LbServer newServer : newServers) {
        String instanceId = newServer.getInstanceId();
        if (!existIds.contains(instanceId)) {
          allServers.add(newServer);
        }
      }
    }
  }

  @Override
  public void markServerDown(@Nonnull LbServer server) {
    reachableServerMap.remove(server.getInstanceId());
  }

  @Nonnull
  @Override
  public List<LbServer> getReachableServers() {
    Collection<LbServer> values = reachableServerMap.values();
    return new ArrayList<>(values);
  }

  @Nonnull
  @Override
  public List<LbServer> getAllServers() {
    return new ArrayList<>(allServers);
  }

  /**
   * 可用性检测
   */
  private void availableBeat() {
    synchronized (this) {
      for (LbServer server : allServers) {
        String instanceId = server.getInstanceId();
        boolean available = server.availableBeat();
        if (available) {
          reachableServerMap.put(instanceId, server);
        } else {
          reachableServerMap.remove(instanceId);
        }
      }
    }
  }
}
