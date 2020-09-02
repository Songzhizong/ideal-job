package cn.sh.ideal.job.scheduler.core.socket.rsocket;

import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.message.payload.LoginMessage;
import cn.sh.ideal.job.common.utils.JsonUtils;
import cn.sh.ideal.job.common.worker.TaskWorker;
import cn.sh.ideal.job.scheduler.core.conf.JobSchedulerProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/9/1
 */
@Controller
public class RSocketServer {
  private static final Logger log = LoggerFactory.getLogger(RSocketServer.class);
  private static final List<RSocketRequester> CLIENTS = new ArrayList<>();
  @Nonnull
  private final LbFactory<TaskWorker> lbFactory;
  @Nonnull
  private final JobSchedulerProperties schedulerProperties;

  public RSocketServer(@Nonnull LbFactory<TaskWorker> lbFactory,
                       @Nonnull JobSchedulerProperties schedulerProperties) {
    this.lbFactory = lbFactory;
    this.schedulerProperties = schedulerProperties;
  }

  /**
   * 客户端与服务端建立连接
   */
  @ConnectMapping("login")
  void login(RSocketRequester requester, @Payload String loginMessage) {
    String propertiesAccessToken = schedulerProperties.getAccessToken();
    LoginMessage message = JsonUtils.parseJson(loginMessage, LoginMessage.class);
    String instanceId = message.getInstanceId();
    String appName = message.getAppName();
    String accessToken = message.getAccessToken();
    int weight = message.getWeight();
    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          if (StringUtils.isNotBlank(propertiesAccessToken)
              && !propertiesAccessToken.equals(accessToken)) {
            log.info("accessToken不合法");
            requester.route("client-receive")
                .data("accessToken不合法")
                .send()
                .subscribe();
            requester.rsocket().dispose();
          } else {
            // Add all new clients to a client list
            log.info("Client: {} CONNECTED.", loginMessage);
            CLIENTS.add(requester);
            // Callback to client, confirming connection
            requester.route("client-status")
                .data("OPEN")
                .retrieveFlux(String.class)
                .doOnNext(s -> log.info("Client: {} Free Memory: {}.", loginMessage, s))
                .subscribe();
          }
        })
        .doOnError(error -> {
          String errMessage = error.getClass().getName() + ": " + error.getMessage();
          log.info("socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          // Remove disconnected clients from the client list
          CLIENTS.remove(requester);
          log.info("Client {} DISCONNECTED: {}", loginMessage, consumer);
        })
        .subscribe();
  }
}
