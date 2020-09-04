package com.zzsong.job.scheduler.core.socket.rsocket;

import com.google.common.collect.ImmutableList;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.loadbalancer.LbServerHolder;
import com.zzsong.job.common.message.payload.LoginMessage;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.scheduler.core.conf.JobSchedulerProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/9/1
 */
@Controller
public class RSocketServer {
    private static final Logger log = LoggerFactory.getLogger(RSocketServer.class);
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
    void login(@Nonnull RSocketRequester requester, @Payload String loginMessage) {
        String propertiesAccessToken = schedulerProperties.getAccessToken();
        LoginMessage message = JsonUtils.parseJson(loginMessage, LoginMessage.class);
        String instanceId = message.getInstanceId();
        String appName = message.getAppName();
        String accessToken = message.getAccessToken();
        int weight = message.getWeight();
        RSocketTaskWorker[] warp = new RSocketTaskWorker[1];
        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    if (StringUtils.isNotBlank(propertiesAccessToken)
                            && !propertiesAccessToken.equals(accessToken)) {
                        log.info("accessToken不合法");
                        requester.route("interrupt")
                                .data("accessToken不合法")
                                .retrieveMono(String.class)
                                .doOnNext(log::info)
                                .subscribe();
                    } else {
                        log.info("Client: {} CONNECTED.", instanceId);
                        RSocketTaskWorker worker
                                = new RSocketTaskWorker(appName, instanceId, requester);
                        warp[0] = worker;
                        worker.setWeight(weight);
                        LbServerHolder<TaskWorker> serverHolder
                                = lbFactory.getServerHolder(appName);
                        serverHolder.addServers(ImmutableList.of(worker));
                    }
                })
                .doOnError(error -> {
                    String errMessage = error.getClass().getName() +
                            ": " + error.getMessage();
                    log.info("socket error: {}", errMessage);
                })
                .doFinally(consumer -> {
                    RSocketTaskWorker worker = warp[0];
                    if (worker != null) {
                        LbServerHolder<TaskWorker> serverHolder
                                = lbFactory.getServerHolder(appName);
                        serverHolder.markServerDown(worker);
                    }
                    log.info("Client {} disconnected: {}", instanceId, consumer);
                })
                .subscribe();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
