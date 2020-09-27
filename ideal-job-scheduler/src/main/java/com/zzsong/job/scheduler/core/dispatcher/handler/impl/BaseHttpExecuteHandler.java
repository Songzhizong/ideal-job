package com.zzsong.job.scheduler.core.dispatcher.handler.impl;

import com.google.common.collect.ImmutableList;
import com.zzsong.job.common.constants.HandleStatusEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.http.HttpMethod;
import com.zzsong.job.common.http.HttpRequest;
import com.zzsong.job.common.http.HttpScriptUtils;
import com.zzsong.job.common.loadbalancer.LbFactory;
import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.loadbalancer.SimpleLbFactory;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.common.utils.ReactorUtils;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandler;
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
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗 on 2020/9/3
 */
public abstract class BaseHttpExecuteHandler implements ExecuteHandler {
  private static final Logger log = LoggerFactory.getLogger(BaseHttpExecuteHandler.class);
  protected static final ConcurrentMap<String, VirtualHttpServer> VIRTUAL_SERVER_MAP
      = new ConcurrentHashMap<>();
  protected static final LbFactory<VirtualHttpServer> LB_FACTORY = new SimpleLbFactory<>();
  // 超时时间5分钟
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
  private final WebClient webClient = ReactorUtils
      .createWebClient(400, 400, READ_TIMEOUT.toMillis());
  @Nonnull
  private final JobInstanceService instanceService;

  protected BaseHttpExecuteHandler(@Nonnull JobInstanceService instanceService) {

    this.instanceService = instanceService;
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  @Override
  public final Mono<Res<Void>> execute(@Nonnull LbServer lbServer,
                                       @Nonnull JobInstance instance,
                                       @Nonnull JobView jobView,
                                       @Nonnull Object executeParam) {
    HttpRequest httpRequest = converterParam(executeParam);
    HttpMethod method = httpRequest.getMethod();
    HttpHeaders headers = httpRequest.getHeaders();
    String body = httpRequest.getBody();

    // 获取请求body
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    Object requestBody = null;
    if (StringUtils.isNoneBlank(body)) {
      if ((StringUtils.startsWith(body, "{")
          && StringUtils.endsWith(body, "}"))
          || (StringUtils.startsWith(body, "[")
          && StringUtils.endsWith(body, "]"))) {
        requestBody = JsonUtils.parseJson(body, Object.class);
      } else if (StringUtils.contains(body, "&")
          || StringUtils.contains(body, "=")) {
        String[] singles = StringUtils.split(body, "&");
        assert singles != null;
        for (String single : singles) {
          String[] sp = StringUtils.split(single, "=", 2);
          if (sp.length == 2) {
            formData.add(sp[0], sp[1]);
          } else {
            log.warn("任务: {} http script body不合法", jobView.getJobId());
          }
        }
      } else {
        requestBody = body;
      }
    }
    VirtualHttpServer server = (VirtualHttpServer) lbServer;
    String hostPort = server.hostPort;
    String schema = httpRequest.getSchema();
    String uri = httpRequest.getUri();
    String queryString = httpRequest.getQueryString();
    StringBuilder urlSb = new StringBuilder(schema)
        .append("://").append(hostPort).append(uri);
    if (StringUtils.isNotBlank(queryString)) {
      urlSb.append("?").append(queryString);
    }
    String requestUri = urlSb.toString();

    TaskResult taskResult = new TaskResult();
    taskResult.setInstanceId(instance.getInstanceId());
    taskResult.setHandleTime(System.currentTimeMillis());
    taskResult.setHandleStatus(HandleStatusEnum.COMPLETE);
    taskResult.setSequence(2);

    log.debug("任务: {} 调用接口: {}", jobView.getJobId(), requestUri);
    WebClient.RequestHeadersSpec<?> clientSpec
        = buildWebClientSpec(method, requestUri, requestBody, headers, formData);
    return clientSpec.retrieve().bodyToMono(String.class)
        .onErrorResume(e -> {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          taskResult.setHandleStatus(HandleStatusEnum.ABNORMAL);
          log.info("调用 {} 异常: {}", hostPort, errMsg);
          return Mono.just(errMsg);
        })
        .flatMap(result -> {
          log.debug("http result = {}", result);
          taskResult.setFinishedTime(System.currentTimeMillis());
          taskResult.setResult(result);
          taskResult.setUpdateTime(DateTimes.now());
          return instanceService.updateByTaskResult(taskResult)
              .map(i -> {
                if (taskResult.getHandleStatus() == HandleStatusEnum.COMPLETE) {
                  return Res.success();
                } else {
                  return Res.err(result);
                }
              });
        });
  }

  @Nonnull
  @Override
  public Object parseExecuteParam(@Nonnull String executeParam) {
    if (StringUtils.isBlank(executeParam)) {
      throw new VisibleException("Http script为空");
    }
    HttpRequest httpRequest;
    try {
      httpRequest = HttpScriptUtils.parse(executeParam);
    } catch (HttpScriptUtils.HttpScriptParseException e) {
      String errMsg = "http script解析异常: " + e.getMessage();
      throw new VisibleException(errMsg);
    }
    return httpRequest;
  }

  @Nonnull
  @Override
  public Mono<List<? extends LbServer>> chooseExecutors(@Nonnull JobView jobView,
                                                        @Nonnull Object executeParam) {
    HttpRequest httpRequest = converterParam(executeParam);
    String ipPort = httpRequest.getIpPort();
    VirtualHttpServer httpServer = VIRTUAL_SERVER_MAP
        .computeIfAbsent(ipPort, k -> new VirtualHttpServer(ipPort));
    return Mono.just(ImmutableList.of(httpServer));
  }

  private HttpRequest converterParam(@Nonnull Object executeParam) {
    return (HttpRequest) executeParam;
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

  static class VirtualHttpServer implements LbServer {
    private final String hostPort;

    VirtualHttpServer(String hostPort) {
      this.hostPort = hostPort;
    }

    @SuppressWarnings("unused")
    public String getHostPort() {
      return hostPort;
    }

    @Nonnull
    @Override
    public String getInstanceId() {
      return hostPort;
    }

    @Override
    public boolean heartbeat() {
      return true;
    }

    @Override
    public int idleBeat(@Nullable Object key) {
      return 0;
    }
  }
}
