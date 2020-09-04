package com.zzsong.job.worker.socket.rsocket;

import com.zzsong.job.common.message.payload.LoginMessage;
import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.worker.RemoteTaskWorker;
import io.rsocket.SocketAcceptor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
public class RSocketRemoteTaskWorker implements RemoteTaskWorker {
    private static final Logger log = LoggerFactory
            .getLogger(RSocketRemoteTaskWorker.class);

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

    private RSocketRequester rsocketRequester;

    public RSocketRemoteTaskWorker(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void setWeight(int weight) {
        this.weight = Math.max(weight, 1);
    }

    public void start() {
        startSocket();
    }

    private synchronized void startSocket() {
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
        this.rsocketRequester = requesterBuilder
                .setupRoute("login")
                .setupData(messageString)
                .rsocketConnector(connector -> connector.acceptor(responder))
                .connectTcp(ip, port)
                .doOnError(throwable -> log.info("", throwable))
                .block();

        assert this.rsocketRequester != null;
        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(error -> log.warn("Connection CLOSED", error))
                .doFinally(consumer -> log.info("Client DISCONNECTED"))
                .subscribe();
    }

    @MessageMapping("client-status")
    public Flux<String> statusUpdate(String status) {
        log.info("Connection {}", status);
        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }

    @Override
    public void taskCallback(@Nonnull TaskCallback callback) {

    }

    @Override
    public void execute(@Nonnull TaskParam param) {

    }

    @Nonnull
    @Override
    public String getInstanceId() {
        return ip + ":" + port;
    }

    @Override
    public boolean heartbeat() {
        return false;
    }
}
