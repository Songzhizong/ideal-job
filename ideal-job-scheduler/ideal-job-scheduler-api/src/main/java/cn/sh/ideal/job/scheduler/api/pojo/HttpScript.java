package cn.sh.ideal.job.scheduler.api.pojo;

import cn.sh.ideal.job.scheduler.api.constant.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Getter
@Setter
public class HttpScript {
  /**
   * 请求方法
   */
  @Nonnull
  @NotNull(message = "HttpMethod不能为空")
  private HttpMethod method;

  /**
   * 请求地址
   */
  @Nonnull
  @NotBlank(message = "请求地址不能为空")
  private String url;
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
