package cn.sh.ideal.job.common.loadbalancer;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SimpleServerHolder<Server extends LbServer> implements LbServerHolder<Server> {
  private static final Logger log = LoggerFactory.getLogger(SimpleServerHolder.class);
  @Getter
  private final String serverName;
  private final ExecutorService heartbeatPool;
  private final BlockingQueue<Boolean> refreshReachableServersQueue
      = new ArrayBlockingQueue<>(1);
  private volatile int cycleStrategy = 0;
  /**
   * 所有服务列表
   */
  private List<Server> allServers = new ArrayList<>();
  /**
   * 可用服务映射, instanceId -> LbServer
   */
  private final ConcurrentMap<String, Server> reachableServerMap
      = new ConcurrentHashMap<>();
  private List<Server> reachableServers = Collections.emptyList();
  private volatile boolean destroyed;


  public SimpleServerHolder(@Nonnull String serverName) {
    this(serverName, 10);
  }

  public SimpleServerHolder(@Nonnull String serverName, int heartbeatIntervalSeconds) {
    this.serverName = serverName;
    // 最多两个线程同时进行心跳测试
    heartbeatPool = new ThreadPoolExecutor(0, 2,
        60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1),
        r -> new Thread(r, "SimpleServerHolder-pool-" + r.hashCode()),
        new ThreadPoolExecutor.DiscardOldestPolicy());

    new Thread(() -> {
      while (!destroyed) {
        try {
          TimeUnit.SECONDS.sleep(heartbeatIntervalSeconds);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        heartbeatPool.execute(this::heartbeat);
      }
    }).start();

    new Thread(() -> {
      while (!destroyed) {
        try {
          Boolean poll = refreshReachableServersQueue.poll(5, TimeUnit.SECONDS);
          if (poll != null) {
            log.info("可用服务列表发生变化, 更新数据...");
            List<Server> lbServers = new ArrayList<>(reachableServerMap.values());
            reachableServers = Collections.unmodifiableList(lbServers);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  @Override
  public void addServers(@Nonnull List<Server> newServers, boolean reachable) {
    synchronized (this) {
      Map<String, Integer> indexMap = new HashMap<>();
      for (int i = 0; i < allServers.size(); i++) {
        final Server server = allServers.get(i);
        final String instanceId = server.getInstanceId();
        indexMap.put(instanceId, i);
      }
      for (Server newServer : newServers) {
        String instanceId = newServer.getInstanceId();
        final Integer integer = indexMap.get(instanceId);
        if (integer == null) {
          // 如果服务之前未注册则直接添加到服务列表
          allServers.add(newServer);
        } else {
          // 服务已注册, 将原有的替换成新的并销毁原对象
          Server server = allServers.get(integer);
          server.destroy();
          allServers.set(integer, newServer);
        }
        if (reachable) {
          reachableServerMap.put(instanceId, newServer);
        }
      }
      refreshReachableServers();
    }
  }

  @Override
  public void markServerReachable(@Nonnull Server server) {
    reachableServerMap.put(server.getInstanceId(), server);
    refreshReachableServers();
  }

  @Override
  public void markServerDown(@Nonnull Server server) {
    reachableServerMap.remove(server.getInstanceId());
    refreshReachableServers();
  }

  @Override
  public synchronized void removeServer(@Nonnull Server server) {
    final String instanceId = server.getInstanceId();
    reachableServerMap.remove(instanceId);
    List<Server> newAllServers = new ArrayList<>(allServers.size());
    for (Server lbServer : allServers) {
      if (!instanceId.equals(lbServer.getInstanceId())) {
        newAllServers.add(lbServer);
      }
    }
    this.allServers = newAllServers;
    refreshReachableServers();
  }

  @Nonnull
  @Override
  public List<Server> getReachableServers() {
    return reachableServers;
  }

  @Nonnull
  @Override
  public List<Server> getAllServers() {
    if (allServers.size() > 0) {
      return Collections.unmodifiableList(new ArrayList<>(allServers));
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * 可用性检测
   */
  @SuppressWarnings("DuplicatedCode")
  private void heartbeat() {
    synchronized (serverName) {
      cycleStrategy = cycleStrategy ^ 1;
      int size = allServers.size();
      int up = 0;
      int down = 0;
      if (cycleStrategy == 0) {
        for (Server server : allServers) {
          String instanceId = server.getInstanceId();
          boolean available;
          try {
            available = server.heartbeat();
          } catch (Exception e) {
            String message = e.getClass().getSimpleName() + ":" + e.getMessage();
            log.info("server: {} heartbeat exception: {}", instanceId, message);
            available = false;
          }
          boolean containsInstance = reachableServerMap.containsKey(instanceId);
          if (available && !containsInstance) {
            up++;
            reachableServerMap.put(instanceId, server);
            refreshReachableServers();
          } else if (!available && containsInstance) {
            down++;
            reachableServerMap.remove(instanceId);
            refreshReachableServers();
          }
        }
      } else {
        for (int i = size - 1; i >= 0; i--) {
          Server server = allServers.get(i);
          String instanceId = server.getInstanceId();
          boolean available;
          try {
            available = server.heartbeat();
          } catch (Exception e) {
            String message = e.getClass().getSimpleName() + ":" + e.getMessage();
            log.info("server: {} heartbeat exception: {}", instanceId, message);
            available = false;
          }
          boolean containsInstance = reachableServerMap.containsKey(instanceId);
          if (available && !containsInstance) {
            up++;
            reachableServerMap.put(instanceId, server);
            refreshReachableServers();
          } else if (!available && containsInstance) {
            down++;
            reachableServerMap.remove(instanceId);
            refreshReachableServers();
          }
        }
      }
      log.debug("对 {} 服务列表执行心跳检测, 当前总服务数: {}, 新标记可达服务数: {}, 不可达服务数: {}, 当前可达服务总数: {}",
          serverName, size, up, down, reachableServerMap.size());
    }
  }

  @Override
  public void destroy() {
    if (!destroyed) {
      destroyed = true;
      heartbeatPool.shutdown();
    }
  }

  @Override
  public boolean isDestroyed() {
    return destroyed;
  }

  private void refreshReachableServers() {
    refreshReachableServersQueue.offer(true);
  }
}
