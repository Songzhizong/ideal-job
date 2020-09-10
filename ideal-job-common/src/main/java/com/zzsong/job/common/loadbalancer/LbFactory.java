package com.zzsong.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡器工厂
 *
 * @author 宋志宗 on 2020/8/20
 */
public interface LbFactory<Server extends LbServer> {

  /**
   * 获取指定应用所有可达的服务
   *
   * @param appName 应用名称
   * @return 所有可达的服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  @Nonnull
  List<Server> getReachableServers(@Nonnull String appName);

  /**
   * 获取所有可达的服务
   *
   * @return appName -> 所有可达的服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  @Nonnull
  Map<String, List<Server>> getReachableServers();

  /**
   * 添加一组服务
   *
   * @param appName    应用名称
   * @param newServers 新加入的服务列表
   * @author on 2020/8/19 23:40
   */
  void addServers(@Nonnull String appName, @Nonnull List<Server> newServers);

  /**
   * 将服务标记为不可达
   *
   * @param appName 应用名称
   * @param server  不可达服务
   * @author 宋志宗 on 2020/8/19 23:40
   */
  void markServerDown(@Nonnull String appName, @Nonnull Server server);

  /**
   * 选取一个server, 默认为轮询策略
   *
   * @param serverName 服务名称
   * @param key        负载均衡器可以使用该对象来确定返回哪个服务
   * @author on 2020/8/20 9:59 上午
   */
  @Nullable
  default Server chooseServer(@Nonnull String serverName,
                              @Nullable Object key) {
    return chooseServer(serverName, key, null);
  }

  /**
   * 选取一个server
   *
   * @param serverName 服务名称
   * @param strategy   负载均衡策略
   * @param key        负载均衡器可以使用该对象来确定返回哪个服务
   * @author on 2020/8/20 9:59 上午
   */
  @Nullable
  Server chooseServer(@Nonnull String serverName,
                      @Nullable Object key,
                      @Nullable LbStrategyEnum strategy);

  /**
   * 获取负载均衡器, 默认为轮询策略
   *
   * @param serverName 服务名称
   * @return LoadBalancer
   * @author on 2020/8/20 9:50 上午
   */
  @Nonnull
  default LoadBalancer<Server> getLoadBalancer(@Nonnull String serverName) {
    return getLoadBalancer(serverName, null);
  }

  /**
   * 获取负载均衡器
   *
   * @param serverName 服务名称
   * @param strategy   负载均衡策略
   * @return LoadBalancer
   * @author on 2020/8/20 9:50 上午
   */
  @Nonnull
  LoadBalancer<Server> getLoadBalancer(@Nonnull String serverName,
                                       @Nullable LbStrategyEnum strategy);

  /**
   * 注册监听器
   *
   * @param listener 监听器
   * @author on 2020/9/9
   */
  void registerEventListener(@Nonnull LbFactoryEventListener<Server> listener);

  /**
   * 当服务数量发生变更时调用方法
   *
   * @param event 变更事件
   * @author on 2020/9/9
   */
  void serverChange(@Nonnull LbFactoryEvent event);

  /**
   * 销毁对象
   */
  void destroy();
}
