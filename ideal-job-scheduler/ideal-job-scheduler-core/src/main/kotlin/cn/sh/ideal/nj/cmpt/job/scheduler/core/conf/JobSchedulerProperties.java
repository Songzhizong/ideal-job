package cn.sh.ideal.nj.cmpt.job.scheduler.core.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component
@ConfigurationProperties("ideal.cmpt.job")
public class JobSchedulerProperties {
  /**
   * 建立连接后等待客户端注册的时间
   */
  private int weightRegisterSeconds = 10;
  @Nonnull
  private String accessToken = "";

  public int getWeightRegisterSeconds() {
    return weightRegisterSeconds;
  }

  public void setWeightRegisterSeconds(int weightRegisterSeconds) {
    this.weightRegisterSeconds = weightRegisterSeconds;
  }

  @Nonnull
  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(@Nonnull String accessToken) {
    this.accessToken = accessToken;
  }
}
