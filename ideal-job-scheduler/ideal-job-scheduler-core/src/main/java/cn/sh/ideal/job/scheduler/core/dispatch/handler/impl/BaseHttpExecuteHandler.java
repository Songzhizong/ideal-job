package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl;

import cn.sh.ideal.job.common.constants.HandleStatusEnum;
import cn.sh.ideal.job.common.constants.RouteStrategyEnum;
import cn.sh.ideal.job.common.constants.TriggerTypeEnum;
import cn.sh.ideal.job.common.exception.VisibleException;
import cn.sh.ideal.job.common.http.HttpMethod;
import cn.sh.ideal.job.common.http.HttpRequest;
import cn.sh.ideal.job.common.http.HttpScriptUtils;
import cn.sh.ideal.job.common.utils.JsonUtils;
import cn.sh.ideal.job.common.utils.ReactorUtils;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance;
import cn.sh.ideal.job.scheduler.core.admin.entity.vo.DispatchJobView;
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService;
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
public abstract class BaseHttpExecuteHandler implements ExecuteHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseHttpExecuteHandler.class);
    private final WebClient webClient = ReactorUtils
            .createWebClient(400, 400, 120_000);
    @Nonnull
    private final JobInstanceService instanceService;
    @Nonnull
    private final ExecutorService jobCallbackThreadPool;

    protected BaseHttpExecuteHandler(@Nonnull JobInstanceService instanceService,
                                     @Nonnull ExecutorService jobCallbackThreadPool) {

        this.instanceService = instanceService;
        this.jobCallbackThreadPool = jobCallbackThreadPool;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public final void execute(@Nonnull JobInstance instance,
                              @Nonnull DispatchJobView jobView,
                              @Nonnull TriggerTypeEnum triggerType,
                              @Nullable String customExecuteParam) {
        String executeParam = customExecuteParam;
        if (executeParam == null) {
            executeParam = jobView.getExecuteParam();
        }
        if (StringUtils.isBlank(executeParam)) {
            log.info("任务: {} Http script为空", jobView.getJobId());
            throw new VisibleException("Http script为空");
        }
        HttpRequest httpRequest;
        try {
            httpRequest = HttpScriptUtils.parse(executeParam);
        } catch (HttpScriptUtils.HttpScriptParseException e) {
            log.info("任务: {} http script解析异常: {}", jobView.getJobId(), e.getMessage());
            throw new VisibleException("http script解析异常: ${e.message}");
        }
        RouteStrategyEnum routeStrategy = jobView.getRouteStrategy();
        String url = httpRequest.getUrl();
        List<String> chooseServer = getAddressList(jobView.getJobId(), url, routeStrategy);
        if (chooseServer.isEmpty()) {
            log.info("任务: {} 选取远程服务为空", jobView.getJobId());
            throw new VisibleException("选取远程服务为空");
        }
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        Object requestBody = null;
        HttpMethod method = httpRequest.getMethod();
        HttpHeaders headers = httpRequest.getHeaders();
        String body = httpRequest.getBody();
        String queryString = httpRequest.getQueryString();
        if (StringUtils.isNoneBlank(body)) {
            if ((StringUtils.startsWith(body, "{")
                    && StringUtils.endsWith(body, "}"))
                    || (StringUtils.startsWith(body, "[")
                    && StringUtils.endsWith(body, "]"))) {
                requestBody = JsonUtils.parseJson(body, Object.class);
            } else {
                String[] singles = StringUtils.split(body, "&");
                assert singles != null;
                for (String single : singles) {
                    String[] sp = StringUtils.split(single, "=", 2);
                    if (sp.length == 2) {
                        formData.add(sp[0], sp[1]);
                    } else {
                        log.info("任务: {} http script body不合法", jobView.getJobId());
                    }
                }
            }
        }
        long handleTime = System.currentTimeMillis();
        if (chooseServer.size() == 1) {
            String uri = chooseServer.get(0);
            String requestUri = StringUtils.isNoneBlank(queryString)
                    ? uri + "?" + queryString : uri;
            WebClient.RequestHeadersSpec<?> clientSpec
                    = buildWebClientSpec(method, requestUri, requestBody, headers, formData);
            Mono<String> bodyToMono = clientSpec.retrieve().bodyToMono(String.class);
            subscriptResponse(bodyToMono, instance, handleTime);
        } else {
            for (String uri : chooseServer) {
                JobInstance jobInstance = JobInstance.createInitialized();
                jobInstance.setParentId(instance.getInstanceId());
                jobInstance.setJobId(jobView.getJobId());
                jobInstance.setExecutorId(jobView.getExecutorId());
                jobInstance.setTriggerType(triggerType);
                jobInstance.setSchedulerInstance(instance.getSchedulerInstance());
                jobInstance.setExecutorHandler(jobView.getExecutorHandler());
                jobInstance.setExecuteParam(executeParam);
                jobInstance.setExecutorInstance(uri);
                String requestUri = StringUtils.isNoneBlank(queryString)
                        ? uri + "?" + queryString : uri;
                WebClient.RequestHeadersSpec<?> clientSpec
                        = buildWebClientSpec(method, requestUri, requestBody, headers, formData);
                Mono<String> bodyToMono = clientSpec.retrieve().bodyToMono(String.class);
                subscriptResponse(bodyToMono, instance, handleTime);
            }
        }
    }

    private void subscriptResponse(@Nonnull Mono<String> bodyToMono,
                                   @Nonnull JobInstance jobInstance,
                                   long handleTime) {
        bodyToMono.onErrorResume(e -> {
            String errMsg = e.getClass().getName() + ": " + e.getMessage();
            log.info("http调度异常: {}", errMsg);
            jobInstance.setHandleStatus(HandleStatusEnum.ABNORMAL);
            return Mono.just(errMsg);
        }).doOnNext(result -> {
            jobInstance.setResult(result);
            if (jobInstance.getHandleStatus() != HandleStatusEnum.ABNORMAL) {
                jobInstance.setHandleStatus(HandleStatusEnum.COMPLETE);
            }
        }).doFinally(signalType -> {
            jobInstance.setHandleTime(handleTime);
            jobInstance.setFinishedTime(System.currentTimeMillis());
            jobInstance.setSequence(2);
            jobCallbackThreadPool.execute(() -> instanceService.saveInstance(jobInstance));
        }).subscribe();
    }

    private WebClient.RequestHeadersSpec<?> buildWebClientSpec(
            @Nonnull HttpMethod method,
            @Nonnull String requestUri,
            @Nullable Object requestBody,
            @Nullable HttpHeaders headers,
            @Nonnull MultiValueMap<String, String> formData) {
        WebClient.RequestHeadersSpec<?> client;
        switch (method) {
            case GET: {
                client = webClient.get().uri(requestUri);
                break;
            }
            case DELETE: {
                client = webClient.delete().uri(requestUri);
                break;
            }
            case POST: {
                WebClient.RequestBodySpec spec = webClient.post().uri(requestUri);
                if (requestBody != null) {
                    spec.body(BodyInserters.fromValue(requestBody));
                } else if (!formData.isEmpty()) {
                    spec.body(BodyInserters.fromFormData(formData));
                }
                client = spec;
                break;
            }
            case PATCH: {
                WebClient.RequestBodySpec spec = webClient.patch().uri(requestUri);
                if (requestBody != null) {
                    spec.body(BodyInserters.fromValue(requestBody));
                } else if (!formData.isEmpty()) {
                    spec.body(BodyInserters.fromFormData(formData));
                }
                client = spec;
                break;
            }
            case PUT: {
                WebClient.RequestBodySpec spec = webClient.put().uri(requestUri);
                if (requestBody != null) {
                    spec.body(BodyInserters.fromValue(requestBody));
                } else if (!formData.isEmpty()) {
                    spec.body(BodyInserters.fromFormData(formData));
                }
                client = spec;
                break;
            }
            default: {
                // 不应该发生的
                String message = "不合法的Http method: " + method.name();
                log.error(message);
                throw new VisibleException(message);
            }
        }
        if (headers != null) {
            client.headers(httpHeaders -> headers.forEach(httpHeaders::put));
        }
        return client;
    }

    protected List<String> getAddressList(long jobId, @Nonnull String scriptUrl,
                                          @Nonnull RouteStrategyEnum routeStrategy) {
        return Collections.singletonList(scriptUrl);
    }
}
