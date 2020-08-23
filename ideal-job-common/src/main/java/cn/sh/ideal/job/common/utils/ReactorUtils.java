package cn.sh.ideal.job.common.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
public final class ReactorUtils {

  @Nonnull
  public static HttpClient createHttpClient(int connectTimeOut,
                                            long writeTimeOut,
                                            long readTimeOut) {
    return HttpClient.create()
        .tcpConfiguration(tcpClient ->
            tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .doOnConnected(connection -> {
                      connection
                          .addHandlerLast(
                              new WriteTimeoutHandler(writeTimeOut, TimeUnit.MILLISECONDS))
                          .addHandlerLast(
                              new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS));
                    }
                )
        );
  }
}
