package com.zzsong.job.common.http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
public enum HttpMethod {
    DELETE,
    GET,
    POST,
    PATCH,
    PUT,
    ;

    @Nullable
    public static HttpMethod valueOfName(@Nonnull String name) {
        String upperCase = name.toUpperCase();
        switch (upperCase) {
            case "DELETE":
                return DELETE;
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PATCH":
                return PATCH;
            case "PUT":
                return PUT;
            default:
                return null;
        }
    }
}
