package com.zzsong.job.worker.socket.rsocket;

import com.zzsong.job.common.message.payload.LoginMessage;
import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.common.worker.RemoteTaskWorker;
import com.zzsong.job.worker.JobExecutor;
import io.rsocket.SocketAcceptor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
public class RSocketRemoteTaskWorker extends Thread implements RemoteTaskWorker {
  private static final Logger log = LoggerFactory.getLogger(RSocketRemoteTaskWorker.class);
  private static final ParameterizedTypeReference<Res<Void>> VOID_RES
      = new ParameterizedTypeReference<Res<Void>>() {
  };

  private final BlockingQueue<Boolean> restartNoticeQueue = new ArrayBlockingQueue<>(1);
  private final int restartDelay = 10;

  /**
   * 调度器程序地址
   */
  private final String ip;
  /**
   * 调度器程序端口
   */
  private final int port;
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
  @Getter
  private int weight = 1;
  @Setter
  private String accessToken;

  private volatile boolean running = false;
  private volatile boolean destroyed = false;

  private RSocketRequester rsocketRequester;

  public RSocketRemoteTaskWorker(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public void setWeight(int weight) {
    this.weight = Math.max(weight, 1);
  }

  private synchronized void startSocket() {
    if (destroyed) {
      log.info("RSocketRemoteTaskWorker is destroyed, schedulerAddress: {}:{}", ip, port);
      return;
    }
    if (running) {
      log.info("RSocketRemoteTaskWorker is running, schedulerAddress: {}:{}", ip, port);
      return;
    }
    RSocketStrategies rSocketStrategies = RSocketConfigure.rsocketStrategies;
    RSocketRequester.Builder requesterBuilder = RSocketConfigure.rSocketRequesterBuilder;
    SocketAcceptor responder
        = RSocketMessageHandler.responder(rSocketStrategies, this);
    LoginMessage loginMessage = new LoginMessage();
    loginMessage.setAppName(appName);
    loginMessage.setInstanceId(workerIp + ":" + workerPort);
    loginMessage.setWeight(weight);
    loginMessage.setAccessToken(accessToken);
    String messageString = loginMessage.toMessageString();
    if (rsocketRequester != null && !rsocketRequester.rsocket().isDisposed()) {
      try {
        this.rsocketRequester.rsocket().dispose();
        this.rsocketRequester = requesterBuilder
            .setupRoute("login")
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .connectTcp(ip, port)
            .doOnError(e -> log.warn("Job scheduler {}:{} Login fail: {}", ip, port, e.getMessage()))
            .doOnNext(r -> log.info("Job scheduler {}:{} login success.", ip, port))
            .block();
      } catch (Exception e) {
        running = false;
        restartSocket();
        return;
      }
    } else {
      try {
        this.rsocketRequester = requesterBuilder
            .setupRoute("login")
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .connectTcp(ip, port)
            .doOnError(e -> log.warn("Job scheduler {}:{} Login fail: {}", ip, port, e.getMessage()))
            .doOnNext(r -> log.info("Job scheduler {}:{} login success.", ip, port))
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
          log.info("Job scheduler socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          running = false;
          log.info("Job scheduler {}:{} 连接断开: {}, {} 秒后尝试重连...",
              ip, port, consumer, restartDelay);
          restartSocket();
        })
        .subscribe();
    running = true;
  }

  public void startWorker() {
    this.start();
  }

  private void restartSocket() {
    restartNoticeQueue.offer(true);
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
          log.info("Restart socket, schedulerAddress: {}:{}", ip, port);
          startSocket();
        }
      } catch (InterruptedException e) {
        // Interrupted
      }
    }
  }

  @Override
  public Mono<Res<Void>> taskCallback(@Nonnull TaskCallback callback) {
    return rsocketRequester.route("task-callback")
        .data(callback)
        .retrieveMono(VOID_RES)
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("task callback result: {}", JsonUtils.toJsonString(res));
          }
        });
  }

  @Override
  @MessageMapping("execute")
  public Mono<Res<Void>> execute(@Nonnull TaskParam param) {
    log.debug("execute: {}", JsonUtils.toJsonString(param));
    return JobExecutor.getExecutor().executeJob(param);
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return ip + ":" + port;
  }

  @Override
  public boolean heartbeat() {
    return running
        && !destroyed
        && rsocketRequester != null
        && !rsocketRequester.rsocket().isDisposed();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void destroy() {
    if (destroyed) {
      return;
    }
    destroyed = true;
    rsocketRequester.rsocket().dispose();
    this.interrupt();
    log.info("RSocketRemoteTaskWorker destroy, schedulerAddress: {}:{}", ip, port);
  }

  @MessageMapping("interrupt")
  public Mono<String> interrupt(String status) {
    log.warn("Job scheduler {}:{} 服务中断: {}, {} 秒后尝试重连...",
        ip, port, status, restartDelay);
    running = false;
    restartSocket();
    return Mono.just("received...");
  }
}
