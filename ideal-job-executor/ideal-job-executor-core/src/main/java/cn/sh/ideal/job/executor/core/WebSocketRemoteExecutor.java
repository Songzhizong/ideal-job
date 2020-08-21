package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.common.executor.RemoteExecutor;
import cn.sh.ideal.job.common.pojo.SocketMessage;
import cn.sh.ideal.job.common.pojo.payload.ExecuteCallbackParam;
import cn.sh.ideal.job.common.pojo.payload.ExecuteParam;
import cn.sh.ideal.job.common.pojo.payload.RegisterParam;
import cn.sh.ideal.job.common.utils.JsonUtils;
import cn.sh.ideal.job.common.utils.ReactorUtils;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class WebSocketRemoteExecutor implements RemoteExecutor {
  private static final Logger log = LoggerFactory.getLogger(WebSocketRemoteExecutor.class);
  private UnicastProcessor<String> directProcessor;
  private Disposable disposable;
  private WebSocketSession socketSession;

  /**
   * 调度器程序地址
   */
  private String schedulerAddress;
  /**
   * 应用名称
   */
  private String appName;
  /**
   * 调度器ip地址
   */
  private String ip;
  /**
   * 调度器端口号
   */
  private int port;
  private int weight = 1;
  private String accessToken;
  private int connectTimeOut = 200;
  private long writeTimeOut = 200;
  private long readTimeOut = 20000;
  private volatile boolean destroyed = false;

  public void setWeight(int weight) {
    this.weight = Math.max(weight, 1);
  }

  private String createRegisterMessage() {
    final RegisterParam param = new RegisterParam();
    param.setWeight(weight);
    param.setAccessToken(accessToken);
    final SocketMessage message = new SocketMessage();
    message.setMessageType(SocketMessage.Type.REGISTER.getCode());
    message.setPayload(JsonUtils.toJsonString(param));
    return JsonUtils.toJsonString(message);
  }

  public void startSocket() {
    String address = schedulerAddress + "/websocket/executor/" +
        appName + "/" + getInstanceId();
    final HttpClient httpClient = ReactorUtils
        .createHttpClient(connectTimeOut, writeTimeOut, readTimeOut);
    final ReactorNettyWebSocketClient socketClient
        = new ReactorNettyWebSocketClient(httpClient);
    final URI uri;
    try {
      uri = new URI(address);
    } catch (URISyntaxException e) {
      log.error("{} URISyntaxException: {}", address, e.getMessage());
      return;
    }
    directProcessor = UnicastProcessor.create();
    disposable = socketClient
        .execute(uri, session -> {
          socketSession = session;
          final Flux<WebSocketMessage> input = session.receive()
              .doOnNext(webSocketMessage -> {
                System.out.println(webSocketMessage.getPayloadAsText());
              })
              .doOnError(throwable -> {
                System.out.println(throwable.getMessage());
              })
              .doOnComplete(() -> {
                System.out.println("结束");
              });
          final Flux<Void> output = directProcessor.map(session::textMessage)
              .flatMap(message -> session.send(Mono.just(message)));
          return Flux.zip(input, output).then()
              .doFinally(signalType -> {
                if (!destroyed) {
                  log.info("连接被关闭, 尝试重新建立连接...");
                  startSocket();
                }
              });
        }).onTerminateDetach()
        .doOnError(throwable -> {
          if (throwable instanceof ReadTimeoutException) {
            log.error("等待来自调度器: {} 的消息超时, 请检测该调度器的运行状态."
                , schedulerAddress);
            return;
          }
          String errMsg = throwable.getClass().getSimpleName() +
              ":" + throwable.getMessage();
          log.info("连接中断, throwable: {}", errMsg);
        })
        .subscribe();
    final String registerMessage = createRegisterMessage();
    sendMessage(registerMessage);
  }

  public void sendMessage(@Nonnull String message) {
    log.debug("Send message to {}, message = {}", schedulerAddress, message);
    directProcessor.onNext(message);
  }

  @Override
  public void callback(ExecuteCallbackParam param) {

  }

  @Override
  public boolean execute(ExecuteParam param) {
    return false;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    if (StringUtils.isBlank(ip) || port < 1) {
      final String message = "ip or port is illegal. ip = " +
          ip + ", port = " + port;
      throw new IllegalArgumentException(message);
    }
    return ip + ":" + port;
  }

  @Override
  public boolean heartbeat() {
    return false;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public int getWeight() {
    return weight;
  }

  @Override
  public void destroy() {
    if (destroyed) {
      return;
    }
    destroyed = true;
    directProcessor.dispose();
    socketSession.close();
    disposable.dispose();
  }
}
