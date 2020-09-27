package com.zzsong.job.scheduler.core.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/20
 */
@Getter
@Setter
@Component
@ConfigurationProperties("ideal.job")
public class JobSchedulerProperties {
  /**
   * 任务实例保存天数
   */
  private int jobInstanceStorageDay = 60;
  @Nonnull
  private String accessToken = "";
  /**
   * 集群配置
   */
  @NestedConfigurationProperty
  private ClusterProperties cluster = new ClusterProperties();

  @NestedConfigurationProperty
  private ThreadPoolProperties blockPool = new ThreadPoolProperties();
}
