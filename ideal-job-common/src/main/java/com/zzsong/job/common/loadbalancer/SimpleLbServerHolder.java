package com.zzsong.job.common.loadbalancer;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;

/**
 * <pre>
 * todo 如果客户端下线后长时间没有上线甚至不再上线,
 *  对象内的定时任务也不会自动结束, 为了防止资源浪费后续需要针对此情况进行优化
 * </pre>
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SimpleLbServerHolder<Server extends LbServer> implements LbServerHolder<Server> {
  private static final Logger log = LoggerFactory.getLogger(SimpleLbServerHolder.class);
  private static final int defaultHeartbeatIntervalSeconds = 20;
  private static ThreadPoolExecutor heartbeatThreadPool;
  private static volatile boolean RUNNING = false;

  static {
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    int corePoolSize = availableProcessors << 2;
    int maximumPoolSize = availableProcessors << 3;
    heartbeatThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(corePoolSize),
        new ThreadFactoryBuilder().setNameFormat("LbServer-heartbeat-%d").build(),
        new ThreadPoolExecutor.DiscardPolicy());
    heartbeatThreadPool.allowCoreThreadTimeOut(true);
  }

  @SuppressWarnings({"unused", "RedundantSuppression"})
  public static void setHeartbeatThreadPool(ThreadPoolExecutor threadPool) {
    if (RUNNING) {
      throw new UnsupportedOperationException("SimpleLbServerHolder已运行, 请尝试在程序初始化过程中进行此项设置");
    }
    SimpleLbServerHolder.heartbeatThreadPool = threadPool;
  }

  @Getter
  private final String serverName;
  private final BlockingQueue<Boolean> refreshReachableServersQueue
      = new ArrayBlockingQueue<>(1);
  private volatile int cycleStrategy = 1;
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


  public SimpleLbServerHolder(@Nonnull String serverName) {
    this(serverName, defaultHeartbeatIntervalSeconds);
  }

  public SimpleLbServerHolder(@Nonnull String serverName, int heartbeatIntervalSeconds) {
    SimpleLbServerHolder.RUNNING = true;
    long heartbeatIntervalMills = heartbeatIntervalSeconds * 1000;
    this.serverName = serverName;

    Thread thread = new Thread(() -> {
      long lastHeartbeatTime = System.currentTimeMillis();
      while (!destroyed) {
        try {
          Boolean poll = refreshReachableServersQueue.poll(5, TimeUnit.SECONDS);
          long currentTimeMillis = System.currentTimeMillis();
          if (currentTimeMillis - lastHeartbeatTime > heartbeatIntervalMills) {
            lastHeartbeatTime = currentTimeMillis;
            heartbeatThreadPool.execute(this::heartbeat);
          }
          if (poll != null) {
            log.debug("可用服务列表发生变化, 更新数据...");
            reachableServers = ImmutableList.copyOf(reachableServerMap.values());
          }
        } catch (InterruptedException e) {
          log.debug("{}", e.getMessage());
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void addServers(@Nonnull List<Server> newServers, boolean reachable) {
    synchronized (this) {
      boolean refreshReachableServers = false;
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
        if (reachable && newServer.heartbeat()) {
          refreshReachableServers = true;
          reachableServerMap.put(instanceId, newServer);
        }
      }
      if (refreshReachableServers) {
        refreshReachableServers();
      }
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
  public void removeServer(@Nonnull Server server) {
    removeServer(server.getInstanceId());
  }

  private synchronized void removeServer(@Nonnull String instanceId) {
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
    final List<Server> temp;
    synchronized (this) {
      cycleStrategy = cycleStrategy ^ 1;
      // 心跳之前先对allServers进行保护性拷贝, 防止循环心跳过程中allServers列表发生变更导致异常
      temp = new ArrayList<>(allServers);
    }
    int size = temp.size();
    int up = 0;
    int down = 0;
    final int reachableServeSize = reachableServerMap.size();
    if (cycleStrategy == 0) {
      for (Server server : temp) {
        String instanceId = server.getInstanceId();
        boolean available;
        try {
          available = server.heartbeat();
        } catch (Exception e) {
          String message = e.getClass().getSimpleName() + ":" + e.getMessage();
          log.info("server: {} heartbeat exception: {}", instanceId, message);
          available = false;
        }
        final Server containsInstance = reachableServerMap.get(instanceId);
        if (available && containsInstance == null) {
          up++;
          reachableServerMap.put(instanceId, server);
          refreshReachableServers();
        } else if (!available && containsInstance != null) {
          down++;
//          reachableServerMap.remove(instanceId);
//          refreshReachableServers();
        }
      }
    } else {
      for (int i = size - 1; i >= 0; i--) {
        Server server = temp.get(i);
        String instanceId = server.getInstanceId();
        boolean available;
        try {
          available = server.heartbeat();
        } catch (Exception e) {
          String message = e.getClass().getSimpleName() + ":" + e.getMessage();
          log.info("server: {} heartbeat exception: {}", instanceId, message);
          available = false;
        }
        final Server containsInstance = reachableServerMap.get(instanceId);
        if (available && containsInstance == null) {
          up++;
          reachableServerMap.put(instanceId, server);
          refreshReachableServers();
        } else if (!available && containsInstance != null) {
          down++;
          reachableServerMap.remove(instanceId);
          refreshReachableServers();
        }
      }
    }
    if (up > 0 || down > 0) {
      log.info("Heartbeat: {} 可达服务列表发生变化, 当前总服务数: {}, 新标记可达服务数: {}, 不可达服务数: {}, 当前可达服务总数: {}",
          serverName, size, up, down, reachableServeSize + up - down);
    }
//    else if (log.isDebugEnabled()) {
//      log.debug("对 {} 服务列表执行心跳检测, 当前总服务数: {}, 新标记可达服务数: {}, 不可达服务数: {}, 当前可达服务总数: {}",
//          serverName, size, up, down, reachableServerMap.size());
//    }
  }

  @Override
  public void destroy() {
    if (!destroyed) {
      destroyed = true;
    }
  }

  private void refreshReachableServers() {
    refreshReachableServersQueue.offer(true);
  }
}
