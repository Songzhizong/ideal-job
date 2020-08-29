package cn.sh.ideal.job.common.http;

import cn.sh.ideal.job.common.utils.JsonUtils;
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
 *    "executorId": 130282956815073280,
 *    "executorHandler": "demoJobHandler",
 *    "executorParam": "demoJobHandler",
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
  public static HttpRequest parse(@Nonnull String script) {
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
        httpRequest.setUrl(strings[0]);
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
//    try (ByteArrayInputStream bis = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
//         InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
//         BufferedReader br = new BufferedReader(isr)) {
//      AtomicBoolean followingIsBody = new AtomicBoolean(false);
//      br.lines().forEach(line -> {
//        if (StringUtils.isBlank(line)) {
//          if (StringUtils.isNotBlank(httpScript.getUrl())) {
//            followingIsBody.set(true);
//          }
//          return;
//        }
//        if (StringUtils.isBlank(httpScript.getUrl())) {
//          String[] split = StringUtils.split(line, " ");
//          if (split.length != 2) {
//            throw new HttpScriptParseException("Illegal method url line");
//          }
//          HttpMethod httpMethod = HttpMethod.valueOfName(split[0]);
//          if (httpMethod == null) {
//            throw new HttpScriptParseException("Illegal http method");
//          }
//          httpScript.setMethod(httpMethod);
//          httpScript.setUrl(split[1]);
//        } else if (!followingIsBody.get()) {
//          Map.Entry<String, String> entry = parseHeaderLine(line);
//          httpHeaders.add(entry.getKey(), entry.getValue());
//        } else {
//          bodyBuilder.append(StringUtils.trim(line));
//        }
//      });
//    } catch (IOException e) {
//      throw new HttpScriptParseException(e.getMessage(), e);
//    }
    httpRequest.setHeaders(httpHeaders);
    httpRequest.setBody(bodyBuilder.toString());
    return httpRequest;
  }

  @Nonnull
  public static String format(@Nonnull HttpRequest httpRequest) {
    StringBuilder scriptBuilder = new StringBuilder();
    scriptBuilder.append(httpRequest.getMethod().name()).append(" ").append(httpRequest.getUrl());
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
  private static Map.Entry<String, String> parseHeaderLine(@Nonnull String headerLine) {
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


  public static class HttpScriptParseException extends RuntimeException {
    public HttpScriptParseException(String message) {
      super(message);
    }

//    public HttpScriptParseException(String message, Throwable cause) {
//      super(message, cause);
//    }
  }

}
