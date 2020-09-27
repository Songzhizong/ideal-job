package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.zzsong.job.scheduler.core.dispatcher.ClusterNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * 集群executor注册表
 * <pre>
 *   保存各executor和集群中各个节点的注册关系
 * </pre>
 *
 * @author 宋志宗 2020/9/9
 */
public interface ClusterRegistry {

  /**
   * 获取机器注册详情
   *
   * @return 注册表详情, 机器节点 -> 应用名称 -> 在线客户端列表
   */
  @Nonnull
  Map<ClusterNode, Map<String, List<String>>> getClusterRegistryDetails();

  /**
   * 删除集群节点
   */
  void removeNode(@Nonnull ClusterNode node);

  /**
   * 刷新节点支持服务列表, 新加入的节点也通过此方法进行注册
   */
  void refreshNode(@Nonnull ClusterNode node, @Nonnull Map<String, List<String>> supportApps);

  /**
   * 通过appName获取可用的集群节点
   *
   * @param appName executor app name
   * @return 可用集群节点
   */
  @Nonnull
  List<ClusterNode> getSupportNodes(@Nonnull String appName);

  /**
   * 获取所有的可用节点
   *
   * @author 宋志宗 on 2020/9/10
   */
  @Nonnull
  List<ClusterNode> getAvailableNodes();

  /**
   * 通过appName查询当前节点是否可用
   *
   * @param appName executor app name
   * @return 当前节点是否可用
   * @author 宋志宗 on 2020/9/10
   */
  boolean isCurrentNodeSupport(@Nonnull String appName);
}
