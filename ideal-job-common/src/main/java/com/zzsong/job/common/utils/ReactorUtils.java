package com.zzsong.job.common.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
public final class ReactorUtils {


    @Nonnull
    public static WebClient createWebClient(int connectTimeOut,
                                            long writeTimeOut,
                                            long readTimeOut) {
        return createWebClientBuilder(connectTimeOut, writeTimeOut, readTimeOut).build();
    }


    @Nonnull
    public static WebClient.Builder createWebClientBuilder(int connectTimeOut,
                                                           long writeTimeOut,
                                                           long readTimeOut) {
        HttpClient httpClient = createHttpClient(connectTimeOut, writeTimeOut, readTimeOut);
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }


    @Nonnull
    public static HttpClient createHttpClient(int connectTimeOut,
                                              long writeTimeOut,
                                              long readTimeOut) {
        return HttpClient.create()
                .tcpConfiguration(tcpClient -> tcpClient
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .doOnConnected(connection -> connection
                                .addHandlerLast(
                                        new WriteTimeoutHandler(writeTimeOut, TimeUnit.MILLISECONDS))
                                .addHandlerLast(
                                        new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS))
                        )
                );
    }
}
