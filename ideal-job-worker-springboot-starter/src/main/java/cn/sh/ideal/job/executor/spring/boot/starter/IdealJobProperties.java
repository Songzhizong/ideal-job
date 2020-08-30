package cn.sh.ideal.job.executor.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Getter
@Setter
@ConfigurationProperties("ideal.job")
public class IdealJobProperties {
  /**
   * 服务应用名称, 如果为空则自动读取 spring.application.name
   */
  private String appName = "";
  /**
   * 调度器验证token
   */
  private String accessToken = "";
  /**
   * 服务权重
   */
  private int weight = 1;
  /**
   * 调度器地址列表: ws://127.0.0.1:8804,ws://127.0.0.1:8805,ws://127.0.0.1:8806
   */
  private String schedulerAddresses = "";

  @NestedConfigurationProperty
  private ThreadPoolProperties executorPool = new ThreadPoolProperties();
  /**
   * 当前服务的ip地址, 为空则会尝试自动获取
   */
  private String ip = "";
  /**
   * 当前服务的端口号, 小于1则会尝试自动获取
   */
  private int port = -1;
  /**
   * 和调度器建立连接的超时时间
   */
  private Duration connectTimeOut = Duration.ofSeconds(2);
  /**
   * 向调度器写入消息的超时时间
   */
  private Duration writeTimeOut = Duration.ofSeconds(1);
  /**
   * 等待调度器写入消息的超时时间
   * <p>该值应该大于调度器定时心跳的时间</p>
   */
  private Duration readTimeOut = Duration.ofSeconds(60);
}
