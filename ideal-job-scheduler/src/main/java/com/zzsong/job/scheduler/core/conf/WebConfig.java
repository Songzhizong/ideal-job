package com.zzsong.job.scheduler.core.conf;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zzsong.job.common.utils.JsonUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import javax.annotation.Nonnull;

/**
 * Created by 宋志宗 on 2020/9/12
 */
@EnableWebFlux
@Configuration
public class WebConfig implements WebFluxConfigurer {

  @Override
  public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
    final SimpleModule javaTimeModule = JsonUtils.getJavaTimeModule();
    final SimpleModule longModule = new SimpleModule()
        .addSerializer(Long.class, ToStringSerializer.instance)
        .addSerializer(Long.TYPE, ToStringSerializer.instance);
    final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(javaTimeModule)
        .registerModule(longModule)
        .findAndRegisterModules();
    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
  }
}
