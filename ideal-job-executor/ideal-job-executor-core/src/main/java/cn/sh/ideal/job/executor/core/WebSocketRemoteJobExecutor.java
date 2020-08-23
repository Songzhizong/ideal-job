package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.common.ParseException;
import cn.sh.ideal.job.common.executor.RemoteJobExecutor;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.message.SocketMessage;
import cn.sh.ideal.job.common.message.payload.*;
import cn.sh.ideal.job.common.utils.JsonUtils;
import cn.sh.ideal.job.common.utils.ReactorUtils;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.Getter;
import lombok.Setter;
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
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public final class WebSocketRemoteJobExecutor extends Thread implements RemoteJobExecutor {
  private static final Logger log = LoggerFactory.getLogger(WebSocketRemoteJobExecutor.class);
  private UnicastProcessor<String> directProcessor;
  private Disposable socketDisposable;
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
  private boolean running = false;
  private volatile boolean destroyed = false;

  public void setWeight(int weight) {
    this.weight = Math.max(weight, 1);
  }

  private String createRegisterMessage() {
    final RegisterParam param = new RegisterParam();
    param.setWeight(weight);
    param.setAccessToken(accessToken);
    final SocketMessage message = new SocketMessage();
    message.setMessageType(MessageType.REGISTER.getCode());
    message.setPayload(JsonUtils.toJsonString(param));
    return JsonUtils.toJsonString(message);
  }

  public synchronized void startSocket() {
    if (destroyed) {
      log.info("WebSocketRemoteJobExecutor is destroyed, schedulerAddress: {}", schedulerAddress);
      return;
    }
    if (running) {
      log.info("WebSocketRemoteJobExecutor is started, schedulerAddress: {}", schedulerAddress);
      return;
    }
    running = true;
    String address = schedulerAddress + "/websocket/executor/" +
        appName + "/" + ip + ":" + port;
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
    socketDisposable = socketClient
        .execute(uri, session -> {
          socketSession = session;
          final Flux<WebSocketMessage> input = session.receive()
              .doOnNext(this::disposeMessage)
              .doOnError(throwable -> {
                String message = throwable.getClass().getSimpleName() +
                    ":" + throwable.getMessage();
                log.info("接收消息出现异常: {}", message);
              })
              .doOnComplete(() -> {
                running = false;
                if (!destroyed) {
                  int delay = 10;
                  log.info("连接断开, {}秒后尝试重连...", delay);
                  restartSocket(delay);
                }
              });
          final Flux<Void> output = directProcessor.map(session::textMessage)
              .flatMap(message -> session.send(Mono.just(message)));
          return Flux.zip(input, output).then()
              .doFinally(signalType -> {
                running = false;
                if (!destroyed) {
                  int delay = 10;
                  log.info("连接被关闭, {}秒后尝试重新建立连接...", delay);
                  restartSocket(delay);
                }
              });
        }).onTerminateDetach()
        .doOnError(throwable -> {
          if (throwable instanceof ReadTimeoutException) {
            log.error("等待来自调度器: {} 的消息超时, 请检测该调度器的运行状态."
                , schedulerAddress);
          } else {
            String errMsg = throwable.getClass().getSimpleName() +
                ":" + throwable.getMessage();
            log.info("连接中断, throwable: {}", errMsg);
          }
        })
        .doFinally(signalType -> {
          running = false;
          if (!destroyed) {
            int delay = 10;
            log.info("WebSocketRemoteJobExecutor terminate, restart after {} seconds, schedulerAddress: {}",
                delay, schedulerAddress);
            restartSocket(delay);
          } else {
            log.info("WebSocketRemoteJobExecutor terminate, schedulerAddress: {}", schedulerAddress);
          }
        }).subscribe();
    final String registerMessage = createRegisterMessage();
    sendMessage(registerMessage);
  }

  public synchronized void restartSocket(int delay) {
    try {
      TimeUnit.SECONDS.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    log.info("Restart socket, schedulerAddress: {}", schedulerAddress);
    startSocket();
  }

  public void sendMessage(@Nonnull String message) {
    log.debug("Send message to {}, message = {}", schedulerAddress, message);
    directProcessor.onNext(message);
  }

  private void disposeMessage(@Nonnull WebSocketMessage webSocketMessage) {
    String message = webSocketMessage.getPayloadAsText();
    // 解析消息
    SocketMessage socketMessage;
    try {
      socketMessage = SocketMessage.parseMessage(message);
    } catch (ParseException e) {
      Throwable cause = e.getCause();
      String errMessage = cause.getClass().getName() + ":" + e.getMessage();
      log.info("解析来自调度器的消息出现异常: {}, message = {}", errMessage, message);
      return;
    }

    String code = socketMessage.getMessageType();
    MessageType messageType = MessageType.valueOfCode(code);
    if (messageType == null) {
      log.warn("未知的消息类型: {}", code);
      return;
    }
    String messagePayload = socketMessage.getPayload();

    switch (messageType) {
      // 执行任务消息
      case EXECUTE_JOB: {
        ExecuteJobParam executeJobParam;
        try {
          executeJobParam = ExecuteJobParam.parseMessage(messagePayload);
        } catch (ParseException e) {
          Throwable cause = e.getCause();
          String errMessage = cause.getClass().getName() + ":" + e.getMessage();
          log.warn("解析ExecuteJobParam出现异常: {}, param = {}", errMessage, messagePayload);
          break;
        }
        executeJob(executeJobParam);
        break;
      }
      case HEARTBEAT: {
        log.debug("接收到调度器: {} 心跳消息", schedulerAddress);
        break;
      }
      case IDLE_BEAT: {
        IdleBeatParam idleBeatParam;
        try {
          idleBeatParam = IdleBeatParam.parseMessage(messagePayload);
        } catch (ParseException e) {
          Throwable cause = e.getCause();
          String errMessage = cause.getClass().getName() + ":" + e.getMessage();
          log.warn("解析IdleBeatParam出现异常: {}, param = {}", errMessage, messagePayload);
          break;
        }
        String jobId = idleBeatParam.getJobId();
        JobExecutor.idleBeat(jobId);
        break;
      }
      default: {

        break;
      }
    }
  }

  @Override
  public void run() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
    startSocket();
  }

  @Override
  public void executeJob(@Nonnull ExecuteJobParam param) {
    JobExecutor.executeJob(param);
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return schedulerAddress;
  }

  @Override
  public boolean heartbeat() {
    // todo 对服务端进行心跳检测
    return true;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    // todo 对服务端进行空闲状态测试
    return 0;
  }

  @Override
  public int getWeight() {
    // todo 获取服务端权重
    return weight;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void destroy() {
    if (destroyed) {
      return;
    }
    log.info("WebSocketRemoteJobExecutor destroy, schedulerAddress: {}", schedulerAddress);
    destroyed = true;
    try {
      directProcessor.dispose();
    } catch (Exception exception) {
      // non
    }
    try {
      socketSession.close();
    } catch (Exception exception) {
      String errMsg = exception.getClass().getName() + ":" + exception.getMessage();
      log.info("socketSession close exception: {}", errMsg);
    }
    try {
      socketDisposable.dispose();
    } catch (Exception exception) {
      String errMsg = exception.getClass().getName() + ":" + exception.getMessage();
      log.info("dispose socket exception: {}", errMsg);
    }
  }

  @Override
  public void executeJobCallback(@Nonnull ExecuteJobCallback callback) {
    String callbackMessage = callback.toMessageString();
    SocketMessage socketMessage = new SocketMessage(ExecuteJobCallback.typeCode, callbackMessage);
    String messageString = socketMessage.toMessageString();
    sendMessage(messageString);
  }

  @Override
  public void idleBeatCallback(@Nonnull IdleBeatCallback callback) {
    String callbackMessage = callback.toMessageString();
    SocketMessage socketMessage = new SocketMessage(IdleBeatCallback.typeCode, callbackMessage);
    String messageString = socketMessage.toMessageString();
    sendMessage(messageString);

  }
}
