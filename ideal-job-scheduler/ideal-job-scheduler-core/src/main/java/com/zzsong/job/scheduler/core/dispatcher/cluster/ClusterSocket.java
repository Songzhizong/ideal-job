package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.scheduler.core.dispatcher.LocalClusterNode;
import com.zzsong.job.scheduler.core.pojo.JobView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗 on 2020/9/10
 */
@Controller
public class ClusterSocket {
  private static final Logger log = LoggerFactory.getLogger(ClusterSocket.class);
  private final ConcurrentMap<String, RSocketRequester> requesterMap = new ConcurrentHashMap<>();

  @Autowired
  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  private LbFactory<TaskWorker> lbFactory;
  private final LocalClusterNode localClusterNode;

  public ClusterSocket(LocalClusterNode localClusterNode) {
    this.localClusterNode = localClusterNode;
  }

  @ConnectMapping(ClusterRoute.CONNECT)
  void connect(@Nonnull RSocketRequester requester, @Payload String connectMessage) {
    ConnectMessage message = ConnectMessage.parseMessage(connectMessage);
    final String instanceId = message.getInstanceId();
    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          log.info("集群节点: {} 建立连接.", instanceId);
          requesterMap.put(instanceId, requester);
        })
        .doOnError(error -> {
          String errMessage = error.getClass().getName() +
              ": " + error.getMessage();
          log.info("socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          log.info("集群节点: {} 断开连接: {}", instanceId, consumer);
          requesterMap.remove(instanceId);
        })
        .subscribe();
  }

  @SuppressWarnings("DuplicatedCode")
  @MessageMapping(ClusterRoute.SUPPORT_APPS)
  public Flux<List<String>> getSupportApps(String instance) {
    log.info("{} connection ", instance);
    return Flux.interval(Duration.ofSeconds(0), Duration.ofSeconds(30)).map(index -> {
      final Map<String, List<TaskWorker>> map = lbFactory.getReachableServers();
      List<String> supportApps = new ArrayList<>();
      map.forEach((appName, list) -> {
        if (list != null && list.size() > 0) {
          supportApps.add(appName);
        }
      });
      return supportApps;
    });
  }

  @MessageMapping(ClusterRoute.DISPATCH)
  public Mono<Res<Void>> dispatch(@Nonnull DispatchData dispatchData) {
    JobView jobView = dispatchData.getJobView();
    TriggerTypeEnum triggerType = dispatchData.getTriggerType();
    String customExecuteParam = dispatchData.getCustomExecuteParam();
    return localClusterNode.dispatch(jobView, triggerType, customExecuteParam);
  }

  public void refreshNodeNotice(List<String> supportApps) {
    log.info("客户端应用列表发生变化, 通知各节点更新数据...");
    requesterMap.forEach((instanceId, requester) ->
        requester.route(ClusterRoute.REFRESH_SUPPORT_NOTICE)
            .data(supportApps)
            .send()
            .subscribe()
    );
  }
}
