package cn.sh.ideal.job.common.loadbalancer;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SimpleServerHolder implements LbServerHolder {
  /**
   * 所有服务列表
   */
  @Nonnull
  private List<LbServer> allServers = new ArrayList<>();
  /**
   * 可用服务映射, instanceId -> LbServer
   */
  @Nonnull
  private final ConcurrentMap<String, LbServer> reachableServerMap = new ConcurrentHashMap<>();
  /**
   * 心跳检测间隔
   */
  private final int intervalSeconds = 5;

  @Override
  public void addServers(@Nonnull List<LbServer> newServers, boolean reachable) {
    synchronized (this) {
      Map<String, Integer> indexMap = new HashMap<>();
      for (int i = 0; i < allServers.size(); i++) {
        final LbServer server = allServers.get(i);
        final String instanceId = server.getInstanceId();
        indexMap.put(instanceId, i);
      }
      for (LbServer newServer : newServers) {
        String instanceId = newServer.getInstanceId();
        final Integer integer = indexMap.get(instanceId);
        if (integer == null) {
          // 如果服务之前未注册则直接添加到服务列表
          allServers.add(newServer);
        } else {
          // 服务已注册, 将原有的替换成新的并销毁原对象
          LbServer server = allServers.get(integer);
          server.destroy();
          allServers.set(integer, newServer);
        }
        if (reachable) {
          reachableServerMap.put(instanceId, newServer);
        }
      }
    }
  }

  @Override
  public void markServerReachable(@Nonnull LbServer server) {
    reachableServerMap.put(server.getInstanceId(), server);
  }

  @Override
  public void markServerDown(@Nonnull LbServer server) {
    reachableServerMap.remove(server.getInstanceId());
  }

  @Override
  public void removeServer(@Nonnull LbServer server) {
    final String instanceId = server.getInstanceId();
    reachableServerMap.remove(instanceId);
    synchronized (this) {
      List<LbServer> newAllServers = new ArrayList<>(Math.max(allServers.size() - 1, 0));
      for (LbServer lbServer : allServers) {
        if (!instanceId.equals(lbServer.getInstanceId())) {
          newAllServers.add(lbServer);
        }
      }
      this.allServers = newAllServers;
    }
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
        boolean available = server.heartbeat();
        if (available) {
          reachableServerMap.put(instanceId, server);
        } else {
          reachableServerMap.remove(instanceId);
        }
      }
    }
  }
}
