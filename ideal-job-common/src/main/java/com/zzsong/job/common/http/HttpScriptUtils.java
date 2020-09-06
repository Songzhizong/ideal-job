package com.zzsong.job.common.http;

import com.zzsong.job.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * http script 工具
 * <p>Http script:</p>
 * <pre>
 *  POST http://{{ip}}:{{port}}/job/update
 *  Content-Type: application/json
 *
 *  {
 *    "jobId": 130301268919844864,
 *    "workerId": 130282956815073280,
 *    "executorHandler": "demoJobHandler",
 *    "executeParam": "demoJobHandler",
 *    "routeStrategy": "WEIGHTED_RANDOM",
 *    "blockStrategy": "SERIAL",
 *    "cron": "0/1 * * * * ?",
 *    "retryCount": 0,
 *    "jobName": "",
 *    "alarmEmail": ""
 *  }
 * </pre>
 *
 * @author 宋志宗
 * @date 2020/8/29
 */
@SuppressWarnings("unused")
public final class HttpScriptUtils {
  private HttpScriptUtils() {
  }

  @Nonnull
  public static HttpRequest parse(@Nonnull String script) throws HttpScriptParseException {
    if (StringUtils.isBlank(script)) {
      throw new HttpScriptParseException("script is blank");
    }
    HttpRequest httpRequest = new HttpRequest();
    StringBuilder bodyBuilder = new StringBuilder();
    HttpHeaders httpHeaders = new HttpHeaders();
    String[] lines = StringUtils.splitPreserveAllTokens(script, "\r\n");
    boolean followingIsBody = false;
    for (String line : lines) {

      if (StringUtils.isBlank(line)) {
        if (StringUtils.isNotBlank(httpRequest.getUrl())) {
          followingIsBody = true;
        }
        continue;
      }
      if (StringUtils.isBlank(httpRequest.getUrl())) {
        String[] split = StringUtils.split(line, " ");
        if (split.length != 2) {
          throw new HttpScriptParseException("Illegal method url line");
        }
        HttpMethod httpMethod = HttpMethod.valueOfName(split[0]);
        if (httpMethod == null) {
          throw new HttpScriptParseException("Illegal http method");
        }
        httpRequest.setMethod(httpMethod);
        String fullUrl = split[1];
        String[] strings = StringUtils.split(fullUrl, "?", 2);
        String url = strings[0];
        if (!StringUtils.startsWith(url, "http")) {
          throw new HttpScriptParseException("Illegal http url");
        }
        httpRequest.setUrl(url);
        parseUrl(url, httpRequest);
        if (strings.length > 1) {
          httpRequest.setQueryString(strings[1]);
        }
      } else if (!followingIsBody) {
        Map.Entry<String, String> entry = parseHeaderLine(line);
        httpHeaders.add(entry.getKey(), entry.getValue());
      } else {
        bodyBuilder.append(StringUtils.trim(line));
      }
    }
    httpRequest.setHeaders(httpHeaders);
    httpRequest.setBody(bodyBuilder.toString());
    return httpRequest;
  }

  private static void parseUrl(@Nonnull String url, @Nonnull HttpRequest httpRequest) {
    String[] split = StringUtils.split(url, "/", 3);
    String schema = split[0];
    String ipPort = split[1];
    httpRequest.setSchema(StringUtils.split(schema, ":", 2)[0]);
    httpRequest.setIpPort(ipPort);
    if (split.length == 3) {
      httpRequest.setUri("/" + split[2]);
    }
  }

  @Nonnull
  public static String format(@Nonnull HttpRequest httpRequest) {
    StringBuilder scriptBuilder = new StringBuilder()
        .append(httpRequest.getMethod().name())
        .append(" ").append(httpRequest.getUrl());
    String queryString = httpRequest.getQueryString();
    if (StringUtils.isNotBlank(queryString)) {
      scriptBuilder.append("?").append(queryString);
    }
    HttpHeaders headers = httpRequest.getHeaders();
    if (headers != null) {
      headers.forEach((name, values) -> {
        for (String value : values) {
          scriptBuilder.append("\r\n")
              .append(name).append(": ").append(value);
        }
      });
    }
    String body = httpRequest.getBody();
    if (StringUtils.isNotBlank(body)) {
      body = body.trim();
      scriptBuilder.append("\r\n\r\n");
      if ((body.startsWith("{") && body.endsWith("}")
          || body.startsWith("[") && body.endsWith("]"))) {
        Object parseJson = JsonUtils.parseJson(body, Object.class);
        String bodyStr = JsonUtils.toJsonString(parseJson, false, true);
        scriptBuilder.append(bodyStr);
      } else {
        scriptBuilder.append(body);
      }
    }
    return scriptBuilder.toString();
  }

  @Nonnull
  private static Map.Entry<String, String> parseHeaderLine(@Nonnull String headerLine)
      throws HttpScriptParseException {
    String[] split = StringUtils.split(headerLine, ":", 2);
    if (split.length != 2) {
      throw new HttpScriptParseException("Illegal header line");
    }
    return new SimpleEntry<>(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
  }

  public static class SimpleEntry<K, V> implements Map.Entry<K, V> {
    @Nonnull
    private final K key;
    @Nonnull
    private V value;

    public SimpleEntry(@Nonnull K key, @Nonnull V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    @Nonnull
    public K getKey() {
      return key;
    }

    @Override
    @Nonnull
    public V getValue() {
      return value;
    }

    @Override
    @Nonnull
    public V setValue(@Nonnull V value) {
      V tmp = this.value;
      this.value = value;
      return tmp;
    }
  }


  public static class HttpScriptParseException extends Exception {
    public HttpScriptParseException(String message) {
      super(message);
    }

//    public HttpScriptParseException(String message, Throwable cause) {
//      super(message, cause);
//    }
  }

}
