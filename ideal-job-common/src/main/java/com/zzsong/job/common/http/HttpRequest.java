package com.zzsong.job.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
@Getter
@Setter
public class HttpRequest {
    /**
     * 请求方法
     */
    @Nonnull
//    @NotNull(message = "HttpMethod不能为空")
    private HttpMethod method;

    /**
     * 请求地址
     */
    @Nonnull
//    @NotBlank(message = "请求地址不能为空")
    private String url;
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
