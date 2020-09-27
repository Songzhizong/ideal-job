package com.zzsong.job.executor.socket.rsocket;

import com.zzsong.job.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.ClassUtils;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/3
 */
public final class RSocketConfigure {
  private static final String PATH_PATTERN_ROUTE_MATCHER_CLASS
      = "org.springframework.web.util.pattern.PathPatternRouteMatcher";
  private static final ObjectMapper xmlMapper = new Jackson2ObjectMapperBuilder()
      .createXmlMapper(false).factory(new CBORFactory()).build();
  private static final ObjectMapper objectMapper = JsonUtils.mapper;
  private static final MediaType[] SUPPORTED_TYPES = {MediaType.APPLICATION_CBOR};


  private static final List<RSocketStrategiesCustomizer> customizers
      = new ArrayList<RSocketStrategiesCustomizer>() {
    {
      add((strategy) -> {
        strategy.decoder(new Jackson2CborDecoder(xmlMapper, SUPPORTED_TYPES));
        strategy.encoder(new Jackson2CborEncoder(xmlMapper, SUPPORTED_TYPES));
      });
      add((strategy) -> {
        strategy.decoder(new Jackson2JsonDecoder(objectMapper, SUPPORTED_TYPES));
        strategy.encoder(new Jackson2JsonEncoder(objectMapper, SUPPORTED_TYPES));
      });
    }
  };

  public static final RSocketStrategies rsocketStrategies = rSocketStrategies();
  public static final RSocketRequester.Builder rSocketRequesterBuilder
      = RSocketRequester.builder().rsocketStrategies(rsocketStrategies);

  @Nonnull
  private static RSocketStrategies rSocketStrategies() {
    RSocketStrategies.Builder builder = RSocketStrategies.builder();
    if (ClassUtils.isPresent(PATH_PATTERN_ROUTE_MATCHER_CLASS, null)) {
      builder.routeMatcher(new PathPatternRouteMatcher());
    }
    customizers.forEach((customizer) -> customizer.customize(builder));
    return builder.build();
  }
}
