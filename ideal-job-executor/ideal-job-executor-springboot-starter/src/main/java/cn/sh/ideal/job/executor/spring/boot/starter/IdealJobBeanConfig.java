package cn.sh.ideal.job.executor.spring.boot.starter;

import cn.sh.ideal.job.executor.core.IdealJobSpringExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Configuration
@EnableConfigurationProperties({IdealJobProperties.class})
public class IdealJobBeanConfig {
  private static final Logger log = LoggerFactory.getLogger(IdealJobBeanConfig.class);

  private final IdealJobProperties properties;

  public IdealJobBeanConfig(IdealJobProperties properties) {
    this.properties = properties;
  }

  @Value("${spring.application.name}")
  private String applicationName;

  @Bean
  @Nullable
  public IdealJobSpringExecutor idealJobSpringExecutor() {
    String appName = properties.getAppName();
    if (StringUtils.isBlank(appName)) {
      appName = applicationName;
    }
    final String accessToken = properties.getAccessToken();
    final int weight = properties.getWeight();
    final String schedulerAddresses = properties.getSchedulerAddresses();
    final String ip = properties.getIp();
    final int port = properties.getPort();
    final long connectTimeOut = properties.getConnectTimeOut().toMillis();
    final long writeTimeOut = properties.getWriteTimeOut().toMillis();
    final long readTimeOut = properties.getReadTimeOut().toMillis();
    if (StringUtils.isBlank(appName)) {
      log.error("ideal.job.app-name 和 spring.application.name 不能同时为空");
      return null;
    }
    if (StringUtils.isBlank(schedulerAddresses)) {
      log.error("schedulerAddresses is blank.");
      return null;
    }
    final IdealJobSpringExecutor executor = new IdealJobSpringExecutor();
    executor.setAccessToken(accessToken);
    executor.setWeight(weight);
    executor.setSchedulerAddresses(schedulerAddresses);
    executor.setAppName(appName);
    executor.setIp(ip);
    executor.setPort(port);
    executor.setConnectTimeOut((int) connectTimeOut);
    executor.setWriteTimeOut(writeTimeOut);
    executor.setReadTimeOut(readTimeOut);
    return executor;
  }
}
