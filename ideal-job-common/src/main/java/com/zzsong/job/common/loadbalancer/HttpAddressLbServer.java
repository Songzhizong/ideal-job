package com.zzsong.job.common.loadbalancer;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Getter
public class HttpAddressLbServer implements LbServer {

  @Nonnull
  private final String host;

  private final int port;

  @Setter
  private int weight = -1;

  private final Consumer<Boolean> heartbeatFunction;

  public HttpAddressLbServer(@Nonnull String host, int port) {
    this.host = host;
    this.port = port;
    this.heartbeatFunction = null;
  }

  public HttpAddressLbServer(@Nonnull String host, int port,
                             @Nonnull Consumer<Boolean> heartbeatFunction) {
    this.host = host;
    this.port = port;
    this.heartbeatFunction = heartbeatFunction;
  }

  public String getHostPort() {
    return host + ":" + port;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return getHostPort();
  }

  @Override
  public boolean heartbeat() {
    return true;
  }

  @Override
  public int getWeight() {
    return Math.max(weight, 1);
  }
}
