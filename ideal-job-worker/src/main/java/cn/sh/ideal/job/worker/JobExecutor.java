package cn.sh.ideal.job.worker;

import cn.sh.ideal.job.common.constants.HandleStatusEnum;
import cn.sh.ideal.job.common.worker.RemoteTaskWorker;
import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.LbServerHolder;
import cn.sh.ideal.job.common.loadbalancer.SimpleLbFactory;
import cn.sh.ideal.job.common.message.payload.TaskCallback;
import cn.sh.ideal.job.common.message.payload.TaskParam;
import cn.sh.ideal.job.worker.handler.IJobHandler;
import cn.sh.ideal.job.worker.handler.JobHandlerFactory;
import cn.sh.ideal.job.worker.socket.ReactorWebSocketRemoteTaskExecutor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Getter
@Setter
public class JobExecutor {
    private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);
    private static final String SCHEDULER_SERVER_NAME = "ideal-job-scheduler";
    private static final LbFactory<RemoteTaskWorker> lbFactory = new SimpleLbFactory<>();
    private static JobExecutor executor;

    private ThreadPoolExecutor executorService;
    private String accessToken;
    private int weight;
    private String schedulerAddresses;
    private String appName;
    private String ip;
    private int port;

    private int corePoolSize = -1;
    private int maximumPoolSize = -1;
    private int poolQueueSize = 200;

    private int connectTimeOutMills = 2000;
    private long writeTimeOutMills = 200;
    private long readTimeOutMills = 120 * 1000;

    private volatile boolean destroyed;
    private final List<RemoteTaskWorker> remoteExecutors = new ArrayList<>();

    @Nonnull
    public static JobExecutor getExecutor() {
        if (JobExecutor.executor == null) {
            throw new RuntimeException("JobExecutor 未初始化");
        }
        return JobExecutor.executor;
    }

    public JobExecutor() {
    }

    public void start() {
        if (StringUtils.isBlank(schedulerAddresses)) {
            log.error("调度器地址为空...");
            return;
        }
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (corePoolSize < 0) {
            corePoolSize = availableProcessors << 3;
        }
        if (maximumPoolSize < 1) {
            maximumPoolSize = availableProcessors << 5;
        }
        BlockingQueue<Runnable> workQueue;
        if (poolQueueSize < 2) {
            workQueue = new SynchronousQueue<>();
        } else {
            workQueue = new ArrayBlockingQueue<>(poolQueueSize);
        }
        executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                60, TimeUnit.SECONDS, workQueue,
                new ThreadFactoryBuilder().setNameFormat("job-executor-pool-%d").build(),
                (r, executor) -> {
                    log.error("任务执行线程池资源不足, 请尝试修改线程数配置");
                    throw new RejectedExecutionException("Task " + r.toString() +
                            " rejected from Job executor thread pool");
                });
        executorService.allowCoreThreadTimeOut(true);
        Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
        initRemoteExecutors();
        JobExecutor.executor = this;
    }

    public void destroy() {
        if (!destroyed) {
            log.info("JobExecutor destroy.");
            destroyed = true;
            for (RemoteTaskWorker remoteExecutor : remoteExecutors) {
                remoteExecutor.destroy();
            }
            executorService.shutdown();
        }
    }


    private void initRemoteExecutors() {
        final String[] addresses = StringUtils
                .split(schedulerAddresses, ",");
        for (String address : addresses) {
            ReactorWebSocketRemoteTaskExecutor executor
                    = new ReactorWebSocketRemoteTaskExecutor(address);
            executor.setAppName(appName);
            executor.setIp(ip);
            executor.setPort(port);
            executor.setWeight(weight);
            executor.setAccessToken(accessToken);
            executor.setConnectTimeOutMills(connectTimeOutMills);
            executor.setWriteTimeOutMills(writeTimeOutMills);
            executor.setReadTimeOutMills(readTimeOutMills);
            executor.start();
            remoteExecutors.add(executor);
        }
        LbServerHolder<RemoteTaskWorker> holder = getServerHolder();
        holder.addServers(remoteExecutors, true);
    }

    public LbServerHolder<RemoteTaskWorker> getServerHolder() {
        return lbFactory.getServerHolder(SCHEDULER_SERVER_NAME);
    }

    @Nullable
    public RemoteTaskWorker chooseRemoteJobExecutor() {
        return lbFactory.chooseServer(SCHEDULER_SERVER_NAME, null);
    }

    @Nullable
    public RemoteTaskWorker chooseRemoteJobExecutor(int retry) {
        if (retry < 1) {
            return chooseRemoteJobExecutor();
        }
        RemoteTaskWorker remoteJobExecutor = null;
        int i = -1;
        while (remoteJobExecutor == null || i < retry) {
            i++;
            remoteJobExecutor = chooseRemoteJobExecutor();
        }
        return remoteJobExecutor;
    }

    public void executeJob(@Nonnull TaskParam param) {
        final String handlerName = param.getExecutorHandler();
        final IJobHandler jobHandler = JobHandlerFactory.get(handlerName);
        if (jobHandler == null) {
            log.error("不存在此jobHandler: {}", handlerName);
            return;
        }
        executorService.execute(() -> {
            int sequence = 0;
            String jobId = param.getJobId();
            long instanceId = param.getInstanceId();
            String executeParam = param.getExecuteParam();
            long executeTime = System.currentTimeMillis();
            RemoteTaskWorker executor = chooseRemoteJobExecutor(2);
            if (executor == null) {
                log.warn("当前没有可用的RemoteJobExecutor");
            } else {
                TaskCallback runningCallback = new TaskCallback();
                runningCallback.setSequence(++sequence);
                runningCallback.setJobId(jobId);
                runningCallback.setInstanceId(instanceId);
                runningCallback.setHandleStatus(HandleStatusEnum.RUNNING.getCode());
                runningCallback.setHandleTime(executeTime);
                executor.taskCallback(runningCallback);
            }

            TaskCallback endCallback = new TaskCallback();
            endCallback.setSequence(++sequence);
            endCallback.setJobId(jobId);
            endCallback.setInstanceId(instanceId);
            endCallback.setHandleStatus(HandleStatusEnum.COMPLETE.getCode());
            endCallback.setHandleTime(executeTime);
            try {
                Object execute = jobHandler.execute(executeParam);
                if (execute != null) {
                    String handleMessage = execute.toString();
                    if (StringUtils.isNotBlank(handleMessage)) {
                        endCallback.setHandleMessage(handleMessage);
                    }
                }
            } catch (Exception exception) {
                String errMsg = exception.getClass().getSimpleName() + ":" + exception.getMessage();
                log.info("Job execute exception: {}", errMsg);
                endCallback.setHandleStatus(HandleStatusEnum.ABNORMAL.getCode());
                endCallback.setHandleMessage(errMsg);
            } finally {
                endCallback.setFinishedTime(System.currentTimeMillis());
                // 任务完成时间尽量返回服务端, 如果当前没有则暂时缓存起来
                if (executor != null) {
                    executor.taskCallback(endCallback);
                }
            }
        });
    }

    public void idleBeat(@Nonnull String jobId) {
        log.debug("{}", jobId);
    }
}
