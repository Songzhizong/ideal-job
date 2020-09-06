package com.zzsong.job.scheduler.core.socket.rsocket;

import com.zzsong.job.common.message.payload.TaskParam;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.common.worker.TaskWorker;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/9/1
 */
public class RSocketTaskWorker implements TaskWorker {
  private static final Logger log = LoggerFactory.getLogger(RSocketTaskWorker.class);
  private static final ParameterizedTypeReference<Res<Void>> VOID_RES
      = new ParameterizedTypeReference<Res<Void>>() {
  };
  @Nonnull
  private final String appName;
  @Nonnull
  private final String instanceId;
  @Nonnull
  private final RSocketRequester requester;
  @Setter
  private int weight = 1;
  private volatile boolean destroyed = false;

  public RSocketTaskWorker(@Nonnull String appName,
                           @Nonnull String instanceId,
                           @Nonnull RSocketRequester requester) {
    this.appName = appName;
    this.instanceId = instanceId;
    this.requester = requester;
  }

  @Override
  public Mono<Res<Void>> execute(@Nonnull TaskParam param) {
    return requester.route("execute")
        .data(param)
        .retrieveMono(VOID_RES)
        .doOnNext(res -> log.debug("响应信息: {}", JsonUtils.toJsonString(res)));
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public boolean heartbeat() {
    return !requester.rsocket().isDisposed();
  }

  @Override
  public int getWeight() {
    return weight;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public void destroy() {
    if (destroyed) {
      return;
    }
    requester.rsocket().dispose();
    log.info("{} -> {} destroy.", appName, instanceId);
    destroyed = true;
  }
}
