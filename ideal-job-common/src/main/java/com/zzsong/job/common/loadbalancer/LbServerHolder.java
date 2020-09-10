package com.zzsong.job.common.loadbalancer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 服务持有者
 *
 * @author 宋志宗 on 2020/8/19
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public interface LbServerHolder<Server extends LbServer> {


  /**
   * 添加一组服务, 并直接标记为可达状态
   *
   * @param newServers 新加入的服务列表
   * @author 宋志宗 on 2020/8/19 23:40
   */
  default void addServers(@Nonnull List<Server> newServers) {
    addServers(newServers, true);
  }

  /**
   * 添加一组服务
   *
   * @param newServers 新加入的服务列表
   * @param reachable  是否尝试标记为可达状态, 如果设为true且心跳测试通过则直接标记为可达状态
   * @author 宋志宗 on 2020/8/19 23:40
   */
  void addServers(@Nonnull List<Server> newServers, boolean reachable);

  /**
   * 将服务标记为可达
   *
   * @param server 可达服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  void markServerReachable(@Nonnull Server server);

  /**
   * 将服务标记为不可达
   *
   * @param server 不可达服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  void markServerDown(@Nonnull Server server);

  /**
   * 将服务标记为不可达
   *
   * @param server 不可达服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  void removeServer(@Nonnull Server server);

  /**
   * 获取所有可达的服务
   *
   * @return 所有可达的服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  @Nonnull
  List<Server> getReachableServers();

  /**
   * 获取所有服务
   *
   * @return 所有已知的服务，包括可达的和不可达的。
   * @author 宋志宗 on 2020/8/19 23:40
   */
  @Nonnull
  List<Server> getAllServers();

  /**
   * 销毁对象
   */
  void destroy();
}
