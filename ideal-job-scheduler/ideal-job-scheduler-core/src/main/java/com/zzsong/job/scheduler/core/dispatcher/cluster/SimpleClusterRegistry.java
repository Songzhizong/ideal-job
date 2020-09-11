package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.google.common.collect.ImmutableList;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.scheduler.core.conf.JobSchedulerConfig;
import com.zzsong.job.scheduler.core.conf.JobSchedulerProperties;
import com.zzsong.job.scheduler.core.dispatcher.ClusterNode;
import com.zzsong.job.scheduler.core.dispatcher.CoreJobDispatcher;
import com.zzsong.job.scheduler.core.dispatcher.LocalClusterNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗 on 2020/9/10
 */
@Component
public class SimpleClusterRegistry implements ClusterRegistry, InitializingBean {
  private static final Logger log = LoggerFactory.getLogger(SimpleClusterRegistry.class);
  private final JobSchedulerConfig config;
  private final LocalClusterNode localClusterNode;
  private final JobSchedulerProperties properties;
  @Autowired
  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  private CoreJobDispatcher coreJobDispatcher;

  private final ConcurrentMap<ClusterNode, List<String>> nodeSupportMapping = new ConcurrentHashMap<>();
  private ConcurrentMap<String, List<ClusterNode>> appSupportMapping = new ConcurrentHashMap<>();
  private Set<String> localSupports = Collections.emptySet();
  @Nullable
  private List<ClusterNode> availableNodes = null;

  public SimpleClusterRegistry(JobSchedulerConfig config,
                               LocalClusterNode localClusterNode,
                               JobSchedulerProperties properties) {
    this.config = config;
    this.localClusterNode = localClusterNode;
    this.properties = properties;
  }

  @Override
  public void removeNode(@Nonnull ClusterNode node) {
    List<String> remove = nodeSupportMapping.remove(node);
    if (remove != null) {
      log.info("remove node: {}", node.getInstanceId());
      availableNodes = null;
      appSupportMapping = new ConcurrentHashMap<>();
    }
  }

  @Override
  public void refreshNode(@Nonnull ClusterNode node,
                          @Nonnull List<String> supportAppList) {
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (node) {
      final List<String> list = nodeSupportMapping.get(node);
      if (list == null) {
        refresh(node, supportAppList);
        return;
      }
      if (supportAppList.size() == 0 && list.size() == 0) {
        return;
      }
      if (list.size() != supportAppList.size()) {
        refresh(node, supportAppList);
        return;
      }
      Set<String> currentSet = new HashSet<>(list);
      Set<String> newSet = new HashSet<>(supportAppList);
      if (currentSet.size() != newSet.size()) {
        refresh(node, supportAppList);
        return;
      }
      for (String s : newSet) {
        if (!currentSet.contains(s)) {
          refresh(node, supportAppList);
          return;
        }
      }
    }
  }

  private void refresh(@Nonnull ClusterNode node, @Nonnull List<String> supportAppList) {
    nodeSupportMapping.put(node, ImmutableList.copyOf(supportAppList));
    if (node.equals(localClusterNode)) {
      localSupports = new HashSet<>(supportAppList);
    }
    availableNodes = null;
    appSupportMapping = new ConcurrentHashMap<>();
    log.info("{} -> support app list refresh: {}", node.getInstanceId(), JsonUtils.toJsonString(supportAppList));
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

  @Override
  public void afterPropertiesSet() {
    String nodes = properties.getCluster().getNodes();
    if (StringUtils.isBlank(nodes)) {
      return;
    }
    String[] nodeAddresses = StringUtils.split(nodes, ",");
    List<RemoteClusterNode> nodeList = new ArrayList<>();
    for (String nodeAddress : nodeAddresses) {
      String[] split = StringUtils.split(nodeAddress, ":");
      if (split.length != 2) {
        log.error("集群配置错误: {}", nodeAddress);
        System.exit(0);
      }
      String ip = split[0];
      int port = Integer.parseInt(split[1]);
      RemoteClusterNode node = new RemoteClusterNode(ip, port, config, this);
      node.start();
      nodeList.add(node);
    }
    if (nodeList.size() > 0) {
      coreJobDispatcher.setClusterEnabled(true);
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        for (RemoteClusterNode node : nodeList) {
          node.dispose();
        }
      }));
    }
  }
}