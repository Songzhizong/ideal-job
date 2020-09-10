package com.zzsong.job.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/8/28
 */
@Getter
@Setter
public class HttpRequest {
  /**
   * 请求方法
   */
  @Nonnull
  private HttpMethod method;
  /**
   * 请求地址, http://127.0.0.1:8080/hello_world
   */
  @Nonnull
  private String url;
  /**
   * http or https
   */
  @Nonnull
  private String schema;
  /**
   * ip:port or domain:port, 127.0.0.1:8080
   */
  @Nonnull
  private String ipPort;
  /**
   * /hello_world
   */
  @Nonnull
  private String uri = "";
  /**
   * Query string
   */
  @Nullable
  private String queryString;
  /**
   * 请求头
   */
  @Nullable
  private HttpHeaders headers;
  /**
   * body
   */
  @Nullable
  private String body;
}
