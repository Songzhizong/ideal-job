package com.zzsong.job.scheduler.core.dispatch.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.RouteStrategyEnum;
import com.zzsong.job.scheduler.core.admin.service.JobExecutorService;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("springCloudHttpExecuteHandler")
public final class SpringCloudBaseHttpExecuteHandler extends BaseHttpExecuteHandler {
    private static final Logger log = LoggerFactory
            .getLogger(SpringCloudBaseHttpExecuteHandler.class);
    @Nullable
    private final SpringClientFactory springClientFactory;
    @Nonnull
    private final JobExecutorService jobExecutorService;
    private final ConcurrentMap<String, VirtualHttpServer> virtualServers
            = new ConcurrentHashMap<>();

    protected SpringCloudBaseHttpExecuteHandler(
            @Nonnull JobInstanceService instanceService,
            @Nonnull ExecutorService jobCallbackThreadPool,
            @Nullable SpringClientFactory springClientFactory,
            @Nonnull JobExecutorService jobExecutorService) {
        super(instanceService, jobCallbackThreadPool);
        this.springClientFactory = springClientFactory;
        this.jobExecutorService = jobExecutorService;
        ExecuteHandlerFactory.register(ExecuteTypeEnum.LB_HTTP_SCRIPT, this);
    }

    @Override
    protected List<String> getAddressList(long jobId, @Nonnull String scriptUrl,
                                          @Nonnull RouteStrategyEnum routeStrategy) {
        if (springClientFactory == null) {
            log.error("springClientFactory 为空, 请检查实发配置注册中心, SpringCloud http script 必须配合注册中心使用");
            throw new UnsupportedOperationException("无法获取spring cloud 注册中心信息");
        }

        return super.getAddressList(jobId, scriptUrl, routeStrategy);
    }

    static class VirtualHttpServer {
        private final String hostPort;

        VirtualHttpServer(String hostPort) {
            this.hostPort = hostPort;
        }

        public String getHostPort() {
            return hostPort;
        }
    }

}
