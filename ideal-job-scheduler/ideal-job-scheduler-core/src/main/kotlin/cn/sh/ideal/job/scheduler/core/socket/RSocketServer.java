package cn.sh.ideal.job.scheduler.core.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

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

  @ConnectMapping("login")
  void login(RSocketRequester requester, @Payload String client) {
    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          // Add all new clients to a client list
          log.info("Client: {} CONNECTED.", client);
          CLIENTS.add(requester);
        })
        .doOnError(error -> {
          // Warn when channels are closed by clients
          log.warn("Channel to client {} CLOSED", client);
        })
        .doFinally(consumer -> {
          // Remove disconnected clients from the client list
          CLIENTS.remove(requester);
          log.info("Client {} DISCONNECTED", client);
        })
        .subscribe();
    // Callback to client, confirming connection
    requester.route("client-status")
        .data("OPEN")
        .retrieveFlux(String.class)
        .doOnNext(s -> log.info("Client: {} Free Memory: {}.", client, s))
        .subscribe();
  }

}
