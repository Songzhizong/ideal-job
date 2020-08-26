package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.common.exception.ParseException;
import cn.sh.ideal.job.common.executor.RemoteJobExecutor;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.message.SocketMessage;
import cn.sh.ideal.job.common.message.payload.*;
import cn.sh.ideal.job.common.utils.JsonUtils;
import cn.sh.ideal.job.common.utils.ReactorUtils;
import io.netty.handler.timeout.ReadTimeoutException;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public final class ReactorWebSocketRemoteJobExecutor extends Thread implements RemoteJobExecutor {
  private static final Logger log = LoggerFactory.getLogger(ReactorWebSocketRemoteJobExecutor.class);
  private static final AtomicInteger atomicInteger = new AtomicInteger(0);
  private UnicastProcessor<String> directProcessor;
  private Disposable socketDisposable;
  private WebSocketSession socketSession;
  private final BlockingQueue<Boolean> restartNoticeQueue = new ArrayBlockingQueue<>(1);
  private final int restartDelay = 10;

  /**
   * 调度器程序地址
   */
  private final String schedulerAddress;
  /**
   * 应用名称
   */
  @Setter
  private String appName;
  /**
   * 当前执行器ip地址
   */
  @Setter
  private String ip;
  /**
   * 当前执行器端口号
   */
  @Setter
  private int port;
  private int weight = 1;
  @Setter
  private String accessToken;
  @Setter
  private int connectTimeOutMills = 200;
  @Setter
  private long writeTimeOutMills = 200;
  @Setter
  private long readTimeOutMills = 120 * 1000;
  private boolean running = false;
  private volatile boolean destroyed = false;

  public ReactorWebSocketRemoteJobExecutor(String schedulerAddress) {
    super("RemoteJobExecutor-" + schedulerAddress + "-" + atomicInteger.getAndIncrement());
    this.schedulerAddress = schedulerAddress;
  }

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
        .createHttpClient(connectTimeOutMills, writeTimeOutMills, readTimeOutMills);
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
                  log.info("连接断开, {}秒后尝试重连...", restartDelay);
                  restartSocket();
                }
              });
          final Flux<Void> output = directProcessor.map(session::textMessage)
              .flatMap(message -> session.send(Mono.just(message)))
              .doOnError(throwable -> {
                String message = throwable.getClass().getSimpleName() +
                    ":" + throwable.getMessage();
                log.info("发送消息出现异常: {}", message);
              });
          return Flux.merge(input, output).then()
              .doFinally(signalType -> {
                running = false;
                if (!destroyed) {
                  log.info("连接被关闭, {}秒后尝试重新建立连接...", restartDelay);
                  restartSocket();
                }
              });
        })
        .onTerminateDetach()
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
            log.info("WebSocketRemoteJobExecutor terminate, restart after {} seconds, schedulerAddress: {}",
                restartDelay, schedulerAddress);
            restartSocket();
          } else {
            log.info("WebSocketRemoteJobExecutor terminate, schedulerAddress: {}", schedulerAddress);
          }
        }).subscribe();
    final String registerMessage = createRegisterMessage();
    sendMessage(registerMessage);
  }

  public void restartSocket() {
    restartNoticeQueue.offer(true);
  }

  public void sendMessage(@Nonnull String message) {
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
//    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
    startSocket();
    while (!destroyed) {
      final Boolean poll;
      try {
        poll = restartNoticeQueue.poll(5, TimeUnit.SECONDS);
        if (poll != null) {
          TimeUnit.SECONDS.sleep(restartDelay);
          log.info("Restart socket, schedulerAddress: {}", schedulerAddress);
          startSocket();
        }
      } catch (InterruptedException e) {
        // Interrupted
      }
    }
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
    socketSession.pingMessage(dataBufferFactory -> dataBufferFactory.allocateBuffer(0));
    return running && !destroyed;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public int getWeight() {
    return 1;
  }

  @SuppressWarnings({"AliDeprecation", "deprecation", "RedundantSuppression"})
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
      // not care
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
    this.interrupt();
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
