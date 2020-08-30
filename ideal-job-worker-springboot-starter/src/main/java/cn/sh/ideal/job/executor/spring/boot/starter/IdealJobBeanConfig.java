package cn.sh.ideal.job.executor.spring.boot.starter;

import cn.sh.ideal.job.common.utils.IpUtil;
import cn.sh.ideal.job.executor.core.SpringJobExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
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

  @Value("${spring.application.name:}")
  private String applicationName;

  @Value("${server.port:-1}")
  private Integer serverPort;

  @Bean
  @Nullable
  public SpringJobExecutor idealJobSpringExecutor(@Nonnull WebServerApplicationContext context) {
    String appName = properties.getAppName();
    if (StringUtils.isBlank(appName)) {
      appName = applicationName;
    }
    final String accessToken = properties.getAccessToken();
    final int weight = properties.getWeight();
    final String schedulerAddresses = properties.getSchedulerAddresses();
    String ip = properties.getIp();
    if (StringUtils.isBlank(ip)) {
      ip = IpUtil.getIp();
    }
    int port = properties.getPort();
    if (port < 1) {
      if (serverPort == null || serverPort < 1) {
        port = context.getWebServer().getPort();
      } else {
        port = serverPort;
      }
    }
    final long connectTimeOutMills = properties.getConnectTimeOut().toMillis();
    final long writeTimeOutMills = properties.getWriteTimeOut().toMillis();
    final long readTimeOutMills = properties.getReadTimeOut().toMillis();
    int corePoolSize = properties.getExecutorPool().getCorePoolSize();
    int maximumPoolSize = properties.getExecutorPool().getMaximumPoolSize();
    int workQueueSize = properties.getExecutorPool().getWorkQueueSize();
    if (StringUtils.isBlank(appName)) {
      log.error("ideal.job.app-name 和 spring.application.name 不能同时为空");
      return null;
    }
    if (StringUtils.isBlank(schedulerAddresses)) {
      log.error("schedulerAddresses is blank.");
      return null;
    }
    final SpringJobExecutor executor = new SpringJobExecutor();
    executor.setAccessToken(accessToken);
    executor.setWeight(weight);
    executor.setSchedulerAddresses(schedulerAddresses);
    executor.setAppName(appName);
    executor.setIp(ip);
    executor.setPort(port);
    executor.setCorePoolSize(corePoolSize);
    executor.setMaximumPoolSize(maximumPoolSize);
    executor.setPoolQueueSize(workQueueSize);
    executor.setConnectTimeOutMills((int) connectTimeOutMills);
    executor.setWriteTimeOutMills(writeTimeOutMills);
    executor.setReadTimeOutMills(readTimeOutMills);
    return executor;
  }
}
