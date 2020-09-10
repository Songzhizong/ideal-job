package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.google.common.collect.ImmutableList;
import com.zzsong.job.scheduler.core.dispatcher.ClusterNode;
import com.zzsong.job.scheduler.core.dispatcher.LocalClusterNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗 on 2020/9/10
 */
public class SimpleClusterRegistry implements ClusterRegistry {
  private final LocalClusterNode localClusterNode;
  private final ConcurrentMap<ClusterNode, List<String>> nodeSupportMapping = new ConcurrentHashMap<>();
  private ConcurrentMap<String, List<ClusterNode>> appSupportMapping = new ConcurrentHashMap<>();
  private Set<String> localSupports = Collections.emptySet();
  @Nullable
  private List<ClusterNode> availableNodes = null;

  public SimpleClusterRegistry(LocalClusterNode localClusterNode) {
    this.localClusterNode = localClusterNode;
  }

  @Override
  public void removeNode(@Nonnull ClusterNode node) {
    nodeSupportMapping.remove(node);
    availableNodes = null;
    appSupportMapping = new ConcurrentHashMap<>();
  }

  @Override
  public void refreshNode(@Nonnull ClusterNode node,
                          @Nonnull List<String> supportAppList) {
    nodeSupportMapping.put(node, ImmutableList.copyOf(supportAppList));
    if (node.equals(localClusterNode)) {
      localSupports = new HashSet<>(supportAppList);
    }
    availableNodes = null;
    appSupportMapping = new ConcurrentHashMap<>();
  }

  @Nonnull
  @Override
  public List<ClusterNode> getSupportNodes(@Nonnull String appName) {
    return appSupportMapping.computeIfAbsent(appName, k -> {
      List<ClusterNode> nodes = new ArrayList<>();
      nodeSupportMapping.forEach((node, apps) -> {
        if (apps.contains(appName)) {
          nodes.add(node);
        }
      });
      return ImmutableList.copyOf(nodes);
    });
  }

  @Nonnull
  @Override
  public List<ClusterNode> getAvailableNodes() {
    if (availableNodes == null) {
      synchronized (this) {
        if (availableNodes == null) {
          availableNodes = ImmutableList.copyOf(nodeSupportMapping.keySet());
        }
      }
    }
    return availableNodes;
  }

  @Override
  public boolean isCurrentNodeSupport(@Nonnull String appName) {
    return localSupports.contains(appName);
  }
}