package com.zzsong.job.scheduler.core.socket.weosocket.handler.impl;

import com.google.common.collect.ImmutableList;
import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.loadbalancer.LbServerHolder;
import com.zzsong.job.common.message.MessageType;
import com.zzsong.job.common.message.SocketMessage;
import com.zzsong.job.common.message.payload.LoginMessage;
import com.zzsong.job.common.message.payload.RegisterCallback;
import com.zzsong.job.common.worker.TaskWorker;
import com.zzsong.job.scheduler.core.conf.JobSchedulerProperties;
import com.zzsong.job.scheduler.core.socket.weosocket.WebsocketTaskWorker;
import com.zzsong.job.scheduler.core.socket.weosocket.handler.MessageHandler;
import com.zzsong.job.scheduler.core.socket.weosocket.handler.MessageHandlerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("registerMessageHandler")
public final class RegisterMessageHandler implements MessageHandler {
    private static final Logger log = LoggerFactory
            .getLogger(RegisterMessageHandler.class);

    @Nonnull
    private final LbFactory<TaskWorker> lbFactory;
    @Nonnull
    private final JobSchedulerProperties properties;

    public RegisterMessageHandler(@Nonnull LbFactory<TaskWorker> lbFactory,
                                  @Nonnull JobSchedulerProperties properties) {
        this.lbFactory = lbFactory;
        this.properties = properties;
        MessageHandlerFactory.register(MessageType.REGISTER, this);
    }

    @Override
    public void execute(@Nonnull WebsocketTaskWorker executor,
                        @Nonnull SocketMessage socketMessage) {
        String payload = socketMessage.getPayload();
        String appName = executor.getAppName();
        String instanceId = executor.getInstanceId();
        if (executor.isRegistered()) {
            log.warn("appName: {}, instanceId: {}, 客户端重复注册, 已忽略该消息", appName, instanceId);
            return;
        }
        LoginMessage loginMessage;
        try {
            loginMessage = LoginMessage.parseMessage(payload);
        } catch (ParseException e) {
            Throwable cause = e.getCause();
            String errMsg = cause.getClass().getName() + ": " + cause.getMessage();
            log.warn("解析 ExecuteJobCallback 出现异常: {}, payload = {}", errMsg, payload);
            return;
        }
        String accessToken = properties.getAccessToken();
        RegisterCallback callback = new RegisterCallback();
        callback.setMessage(socketMessage.getMessageId());
        if (StringUtils.isNotBlank(accessToken)) {
            String registerToken = loginMessage.getAccessToken();
            if (!accessToken.equals(registerToken)) {
                log.warn("appName: {}, instanceId: {} 请求token不合法: {}",
                        appName, instanceId, registerToken);
                callback.setSuccess(false);
                callback.setMessage("accessToken不合法");
                SocketMessage callbackMessage = new SocketMessage(
                        RegisterCallback.typeCode, callback.toMessageString());
                executor.sendMessage(callbackMessage.toMessageString());
                executor.destroy();
                return;
            }
        }
        executor.setWeight(loginMessage.getWeight());
        executor.setRegistered(true);
        LbServerHolder<TaskWorker> serverHolder = lbFactory.getServerHolder(appName);
        serverHolder.addServers(ImmutableList.of(executor));
        log.info("客户端完成注册, appName: {}, instanceId: {}, 注册参数: {}",
                appName, instanceId, payload);
        callback.setSuccess(true);
        callback.setMessage("success");
        SocketMessage callbackMessage = new SocketMessage(
                RegisterCallback.typeCode, callback.toMessageString());
        executor.sendMessage(callbackMessage.toMessageString());
    }
}
