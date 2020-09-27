package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.scheduler.core.conf.JobSchedulerConfig;
import com.zzsong.job.scheduler.core.dispatcher.ClusterNode;
import com.zzsong.job.scheduler.core.pojo.JobView;
import io.rsocket.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 远程集群调度器
 * <p>通过此调度器将任务交给集群中的其他节点完成调度</p>
 *
 * @author 宋志宗 2020/9/9
 */
@SuppressWarnings("unused")
public class RemoteClusterNode extends Thread implements ClusterNode {
  private static final Logger log = LoggerFactory.getLogger(RemoteClusterNode.class);
  private static final ParameterizedTypeReference<Res<Void>> VOID_RES
      = new ParameterizedTypeReference<Res<Void>>() {
  };
  private static final ParameterizedTypeReference<Map<String, List<String>>> STRING_LIST_MAP_RES
      = new ParameterizedTypeReference<Map<String, List<String>>>() {
  };
  private final BlockingQueue<Boolean> restartNoticeQueue
      = new ArrayBlockingQueue<>(1);
  private final int restartDelay = 10;
  /**
   * 调度器程序地址
   */
  @Nonnull
  private final String ip;
  /**
   * 调度器程序端口
   */
  private final int port;
  @Nonnull
  private final String ipPort;
  @Nonnull
  private final ClusterRegistry clusterRegistry;
  private final JobSchedulerConfig config;

  private volatile boolean running = false;
  private volatile boolean destroyed = false;

  private RSocketRequester rsocketRequester;

  public RemoteClusterNode(@Nonnull String ip, int port,
                           @Nonnull JobSchedulerConfig config,
                           @Nonnull ClusterRegistry clusterRegistry) {
    this.ip = ip;
    this.port = port;
    this.config = config;
    this.clusterRegistry = clusterRegistry;
    this.ipPort = ip + ":" + port;

  }

  @Override
  public Mono<Res<Void>> dispatch(@Nonnull JobView jobView,
                                  @Nonnull TriggerTypeEnum triggerType,
                                  @Nullable String customExecuteParam) {
    final DispatchData dispatchData = new DispatchData();
    dispatchData.setJobView(jobView);
    dispatchData.setTriggerType(triggerType);
    dispatchData.setCustomExecuteParam(customExecuteParam);
    return rsocketRequester.route(ClusterRoute.DISPATCH)
        .data(dispatchData)
        .retrieveMono(VOID_RES)
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("{} -> dispatch result: {}", ipPort, JsonUtils.toJsonString(res));
          }
        });
  }

  private synchronized void startSocket() {
    if (destroyed) {
      log.info("{} -> RemoteClusterDispatcher is destroyed", ipPort);
      return;
    }
    if (running) {
      log.info("{} -> RemoteClusterDispatcher is running", ipPort);
      return;
    }
    RSocketStrategies rSocketStrategies = RSocketConfigure.rsocketStrategies;
    RSocketRequester.Builder requesterBuilder = RSocketConfigure.rSocketRequesterBuilder;
    SocketAcceptor responder
        = RSocketMessageHandler.responder(rSocketStrategies, this);
    final ConnectMessage message = new ConnectMessage();
    message.setInstanceId(config.getIpPort());
    final String messageString = message.toMessageString();
    if (rsocketRequester != null && !rsocketRequester.rsocket().isDisposed()) {
      try {
        this.rsocketRequester.rsocket().dispose();
        this.rsocketRequester = requesterBuilder
            .setupRoute(ClusterRoute.CONNECT)
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .connectTcp(ip, port)
            .doOnError(e -> log.warn("{} -> RemoteClusterDispatcher Login fail: ", ipPort, e))
            .doOnNext(r -> log.info("{} -> RemoteClusterDispatcher login success.", ipPort))
            .block();
      } catch (Exception e) {
        running = false;
        restartSocket();
        return;
      }
    } else {
      try {
        this.rsocketRequester = requesterBuilder
            .setupRoute(ClusterRoute.CONNECT)
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .connectTcp(ip, port)
            .doOnError(e -> log.warn("{} -> RemoteClusterDispatcher Login fail: ", ipPort, e))
            .doOnNext(r -> log.info("{} -> RemoteClusterDispatcher login success.", ipPort))
            .block();
      } catch (Exception e) {
        running = false;
        restartSocket();
        return;
      }
    }
    assert this.rsocketRequester != null;
    this.rsocketRequester.rsocket()
        .onClose()
        .doOnError(error -> {
          String errMessage = error.getClass().getSimpleName() +
              ": " + error.getMessage();
          log.info("{} -> RemoteClusterDispatcher socket error: {}", ipPort, errMessage);
        })
        .doFinally(consumer -> {
          running = false;
          log.info("{} -> RemoteClusterDispatcher 连接断开: {}, {} 秒后尝试重连...",
              ipPort, consumer, restartDelay);
          restartSocket();
        })
        .subscribe();
    this.rsocketRequester.route(ClusterRoute.SUPPORT_APPS)
        .data(config.getIpPort())
        .retrieveFlux(STRING_LIST_MAP_RES)
        .doOnNext(list -> clusterRegistry.refreshNode(this, list))
        .subscribe();
    running = true;
  }

  private void restartSocket() {
    clusterRegistry.removeNode(this);
    restartNoticeQueue.offer(true);
  }

  @MessageMapping(ClusterRoute.REFRESH_SUPPORT_NOTICE)
  public Mono<Void> refreshSupportList(Map<String, List<String>> supportApps) {
    clusterRegistry.refreshNode(this, supportApps);
    return Mono.empty();
  }

  @Override
  public void run() {
    startSocket();
    while (!destroyed) {
      final Boolean poll;
      try {
        poll = restartNoticeQueue.poll(5, TimeUnit.SECONDS);
        if (poll != null) {
          TimeUnit.SECONDS.sleep(restartDelay);
          log.info("{} -> Restart RemoteClusterDispatcher", ipPort);
          startSocket();
        }
      } catch (InterruptedException e) {
        // Interrupted
      }
    }
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return ipPort;
  }

  @Override
  public boolean heartbeat() {
    return running
        && !destroyed
        && rsocketRequester != null
        && !rsocketRequester.rsocket().isDisposed();
  }

  @Override
  public int getWeight() {
    return 1;
  }

  @Override
  public void dispose() {
    if (destroyed) {
      return;
    }
    destroyed = true;
    rsocketRequester.rsocket().dispose();
    this.interrupt();
    log.info("{} -> RemoteClusterDispatcher destroy", ipPort);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RemoteClusterNode that = (RemoteClusterNode) o;
    return ipPort.equals(that.ipPort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipPort);
  }
}
