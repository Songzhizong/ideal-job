package com.zzsong.job.scheduler.core.dispatcher.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.loadbalancer.LbStrategyEnum;
import com.zzsong.job.common.loadbalancer.LoadBalancer;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.scheduler.core.admin.service.JobExecutorService;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandlerFactory;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.pojo.JobExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/3
 */
@Component("springCloudHttpExecuteHandler")
public final class SpringCloudHttpExecuteHandler extends BaseHttpExecuteHandler {
  private static final Logger log = LoggerFactory
      .getLogger(SpringCloudHttpExecuteHandler.class);

  @Nonnull
  private final JobExecutorService jobExecutorService;
  @Nullable
  private final DiscoveryClient discoveryClient;

  protected SpringCloudHttpExecuteHandler(
      @Nonnull JobInstanceService instanceService,
      @Nonnull JobExecutorService jobExecutorService,
      @Nullable DiscoveryClient discoveryClient) {
    super(instanceService);
    this.jobExecutorService = jobExecutorService;
    this.discoveryClient = discoveryClient;
    ExecuteHandlerFactory.register(ExecuteTypeEnum.LB_HTTP, this);
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  @Override
  public Mono<List<? extends LbServer>> chooseExecutors(@Nonnull JobView jobView,
                                                        @Nonnull Object executeParam) {
    if (discoveryClient == null) {
      return Mono.error(new VisibleException("DiscoveryClient not found, nonsupport lb http script"));
    }
    long jobId = jobView.getJobId();
    long executorId = jobView.getExecutorId();
    RouteStrategyEnum routeStrategy = jobView.getRouteStrategy();
    Mono<Optional<JobExecutor>> executorMono = jobExecutorService.loadById(executorId);
    return executorMono.flatMap(executorOptional -> {
      if (!executorOptional.isPresent()) {
        log.info("任务: {} 调度失败, 执行器: {} 不存在", jobId, executorId);
        return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND,
            "执行器: " + executorId + "不存在"));
      }
      JobExecutor executor = executorOptional.get();
      String appName = executor.getAppName();
      if (StringUtils.isBlank(appName)) {
        log.info("任务: {} 调度失败, 执行器: {} 应用名称为空", jobId, executorId);
        return Mono.error(new VisibleException("执行器应用名称为空"));
      }
      List<ServiceInstance> instances = discoveryClient.getInstances(appName);
      List<VirtualHttpServer> virtualHttpServers = instances.stream().map(instance -> {
        String host = instance.getHost();
        int port = instance.getPort();
        String hostPort = host + ":" + port;
        return VIRTUAL_SERVER_MAP
            .computeIfAbsent(hostPort, k -> new VirtualHttpServer(hostPort));
      }).collect(Collectors.toList());
      if (virtualHttpServers.isEmpty()) {
        log.info("执行器: {} 当前没有可用的实例", appName);
        return Mono.error(new VisibleException("执行器: " + appName + " 当前没有可用的实例"));
      }
      if (routeStrategy == RouteStrategyEnum.BROADCAST) {
        return Mono.just(virtualHttpServers);
      } else {
        LbStrategyEnum lbStrategy = routeStrategy.getLbStrategy();
        LoadBalancer<VirtualHttpServer> loadBalancer;
        if (lbStrategy == null) {
          loadBalancer = LB_FACTORY.getLoadBalancer(appName);
        } else {
          loadBalancer = LB_FACTORY.getLoadBalancer(appName, lbStrategy);
        }
        final VirtualHttpServer chooseServer = loadBalancer.chooseServer(jobId, virtualHttpServers);
        if (chooseServer == null) {
          log.info("执行器: {} 选取实例为空", appName);
          return Mono.error(new VisibleException("执行器选取实例为空"));
        }
        return Mono.just(Collections.singletonList(chooseServer));
      }
    });
  }
}
