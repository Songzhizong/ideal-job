package cn.sh.ideal.job.common.loadbalancer;

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
  private final ExecutorService executorService;
  private final BlockingQueue<Boolean> refreshReachableServersQueue
      = new ArrayBlockingQueue<>(1);

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

  public SimpleServerHolder(int heartbeatIntervalSeconds) {
    int processors = Runtime.getRuntime().availableProcessors();
    executorService = new ThreadPoolExecutor(1, processors,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        r -> new Thread(r, "SimpleServerHolder-pool-" + r.hashCode()),
        new ThreadPoolExecutor.CallerRunsPolicy());
    new Thread(() -> {
      while (!destroyed) {
        try {
          TimeUnit.SECONDS.sleep(heartbeatIntervalSeconds);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        executorService.submit(this::heartbeat);
      }
    }).start();

    new Thread(() -> {
      while (!destroyed) {
        try {
          Boolean poll = refreshReachableServersQueue.poll(5, TimeUnit.SECONDS);
          if (poll != null) {
            List<Server> lbServers = new ArrayList<>(reachableServerMap.values());
            reachableServers = Collections.unmodifiableList(lbServers);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public SimpleServerHolder() {
    this(10);
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
    synchronized (this) {
      reachableServerMap.put(server.getInstanceId(), server);
      refreshReachableServers();
    }
  }

  @Override
  public void markServerDown(@Nonnull Server server) {
    synchronized (this) {
      reachableServerMap.remove(server.getInstanceId());
      refreshReachableServers();
    }
  }

  @Override
  public void removeServer(@Nonnull Server server) {
    final String instanceId = server.getInstanceId();
    reachableServerMap.remove(instanceId);
    synchronized (this) {
      List<Server> newAllServers = new ArrayList<>(allServers.size());
      for (Server lbServer : allServers) {
        if (!instanceId.equals(lbServer.getInstanceId())) {
          newAllServers.add(lbServer);
        }
      }
      this.allServers = newAllServers;
      refreshReachableServers();
    }
  }

  @Nonnull
  @Override
  public List<Server> getReachableServers() {
    return reachableServers;
  }

  @Nonnull
  @Override
  public List<Server> getAllServers() {
    return new ArrayList<>(allServers);
  }

  /**
   * 可用性检测
   */
  private void heartbeat() {
    synchronized (this) {
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
          reachableServerMap.put(instanceId, server);
          refreshReachableServers();
        } else if (!available && containsInstance) {
          reachableServerMap.remove(instanceId);
          refreshReachableServers();
        }
      }
    }
  }

  @Override
  public void destroy() {
    if (!destroyed) {
      destroyed = true;
      executorService.shutdown();
    }
  }

  private void refreshReachableServers() {
    refreshReachableServersQueue.offer(true);
  }
}
