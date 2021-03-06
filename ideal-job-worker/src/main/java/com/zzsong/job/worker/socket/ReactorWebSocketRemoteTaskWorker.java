package com.zzsong.job.worker.socket;

import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.message.payload.IdleBeatParam;
import com.zzsong.job.common.message.payload.LoginMessage;
import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.worker.RemoteTaskWorker;
import com.zzsong.job.common.message.MessageType;
import com.zzsong.job.common.message.SocketMessage;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.common.utils.ReactorUtils;
import com.zzsong.job.worker.JobExecutor;
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
 * @author 宋志宗 on 2020/8/20
 */
public final class ReactorWebSocketRemoteTaskWorker extends Thread implements RemoteTaskWorker {
  private static final Logger log = LoggerFactory
      .getLogger(ReactorWebSocketRemoteTaskWorker.class);
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
  private String workerIp;
  /**
   * 当前执行器端口号
   */
  @Setter
  private int workerPort;
  private int weight = 1;
  @Setter
  private String accessToken;
  @Setter
  private int connectTimeOutMills = 200;
  @Setter
  private long writeTimeOutMills = 200;
  @Setter
  private long readTimeOutMills = 120 * 1000;
  private volatile boolean running = false;
  private volatile boolean destroyed = false;

  public ReactorWebSocketRemoteTaskWorker(String schedulerAddress) {
    super("RemoteJobExecutor-" + schedulerAddress + "-" + atomicInteger.getAndIncrement());
    this.schedulerAddress = schedulerAddress;
    this.setDaemon(true);
  }

  public void setWeight(int weight) {
    this.weight = Math.max(weight, 1);
  }

  private String createRegisterMessage() {
    final LoginMessage param = new LoginMessage();
    param.setWeight(weight);
    param.setAccessToken(accessToken);
    final SocketMessage message = new SocketMessage();
    message.setMessageType(MessageType.REGISTER.getCode());
    message.setPayload(JsonUtils.toJsonString(param));
    return JsonUtils.toJsonString(message);
  }

  private synchronized void startSocket() {
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
        appName + "/" + workerIp + ":" + workerPort;
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
            log.info("WebSocketRemoteJobExecutor terminate, schedulerAddress: {}",
                schedulerAddress);
          }
        }).subscribe();
    final String registerMessage = createRegisterMessage();
    sendMessage(registerMessage);
  }

  private void restartSocket() {
    restartNoticeQueue.offer(true);
  }

  private void sendMessage(@Nonnull String message) {
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
        TaskParam taskParam;
        try {
          taskParam = TaskParam.parseMessage(messagePayload);
        } catch (ParseException e) {
          Throwable cause = e.getCause();
          String errMessage = cause.getClass().getName() + ":" + e.getMessage();
          log.warn("解析ExecuteJobParam出现异常: {}, param = {}",
              errMessage, messagePayload);
          break;
        }
        execute(taskParam);
        break;
      }
      case IDLE_BEAT: {
        IdleBeatParam idleBeatParam;
        try {
          idleBeatParam = IdleBeatParam.parseMessage(messagePayload);
        } catch (ParseException e) {
          Throwable cause = e.getCause();
          String errMessage = cause.getClass().getName() + ":" + e.getMessage();
          log.warn("解析IdleBeatParam出现异常: {}, param = {}",
              errMessage, messagePayload);
          break;
        }
        String jobId = idleBeatParam.getJobId();
        JobExecutor.getExecutor().idleBeat(jobId);
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
  public Mono<Res<Void>> execute(@Nonnull TaskParam param) {
    return JobExecutor.getExecutor().executeJob(param);
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return schedulerAddress;
  }

  @Override
  public boolean heartbeat() {
    socketSession.pingMessage(dataBufferFactory ->
        dataBufferFactory.allocateBuffer(0));
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
  public void dispose() {
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
  public Mono<Res<Void>> taskCallback(@Nonnull TaskCallback callback) {
    String callbackMessage = callback.toMessageString();
    SocketMessage socketMessage
        = new SocketMessage(TaskCallback.typeCode, callbackMessage);
    String messageString = socketMessage.toMessageString();
    sendMessage(messageString);
    return Mono.just(Res.success());
  }
}
