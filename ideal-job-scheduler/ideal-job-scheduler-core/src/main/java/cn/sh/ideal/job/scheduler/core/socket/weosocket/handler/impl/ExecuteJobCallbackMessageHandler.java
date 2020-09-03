package cn.sh.ideal.job.scheduler.core.socket.weosocket.handler.impl;

import cn.sh.ideal.job.common.exception.ParseException;
import cn.sh.ideal.job.common.message.MessageType;
import cn.sh.ideal.job.common.message.SocketMessage;
import cn.sh.ideal.job.common.message.payload.TaskCallback;
import cn.sh.ideal.job.scheduler.core.dispatch.JobDispatch;
import cn.sh.ideal.job.scheduler.core.socket.weosocket.WebsocketTaskWorker;
import cn.sh.ideal.job.scheduler.core.socket.weosocket.handler.MessageHandler;
import cn.sh.ideal.job.scheduler.core.socket.weosocket.handler.MessageHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("executeJobCallbackMessageHandler")
public final class ExecuteJobCallbackMessageHandler implements MessageHandler {
    private static final Logger log = LoggerFactory
            .getLogger(ExecuteJobCallbackMessageHandler.class);

    @Nonnull
    private final JobDispatch jobDispatch;
    @Nonnull
    private final ExecutorService jobCallbackThreadPool;

    public ExecuteJobCallbackMessageHandler(@Nonnull JobDispatch jobDispatch,
                                            @Nonnull ExecutorService jobCallbackThreadPool) {
        this.jobDispatch = jobDispatch;
        this.jobCallbackThreadPool = jobCallbackThreadPool;
        MessageHandlerFactory.register(MessageType.EXECUTE_JOB_CALLBACK, this);
    }

    @Override
    public void execute(@Nonnull WebsocketTaskWorker executor,
                        @Nonnull SocketMessage socketMessage) {
        String payload = socketMessage.getPayload();
        TaskCallback taskCallback;
        try {
            taskCallback = TaskCallback.parseMessage(payload);
        } catch (ParseException e) {
            Throwable cause = e.getCause();
            String errMsg = cause.getClass().getName() + ": " + cause.getMessage();
            log.warn("解析 ExecuteJobCallback 出现异常: {}, payload = {}", errMsg, payload);
            return;
        }
        jobCallbackThreadPool.execute(() -> jobDispatch.dispatchCallback(taskCallback));
    }
}
