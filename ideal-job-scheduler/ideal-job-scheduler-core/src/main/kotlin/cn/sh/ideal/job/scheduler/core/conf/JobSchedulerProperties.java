package cn.sh.ideal.job.scheduler.core.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Component
@ConfigurationProperties("ideal.job")
public class JobSchedulerProperties {
  private String lockTable = "job_lock";
  private String scheduleLockName = "schedule_lock";
  /**
   * 建立连接后等待客户端注册的时间
   */
  private int weightRegisterSeconds = 10;
  @Nonnull
  private String accessToken = "";

  @NestedConfigurationProperty
  private ThreadPoolProperties executeJobCallbackPool = new ThreadPoolProperties();

  @NestedConfigurationProperty
  private ThreadPoolProperties cronJobTriggerPool = new ThreadPoolProperties();

  public String getLockTable() {
    return lockTable;
  }

  public void setLockTable(String lockTable) {
    this.lockTable = lockTable;
  }

  public String getScheduleLockName() {
    return scheduleLockName;
  }

  public void setScheduleLockName(String scheduleLockName) {
    this.scheduleLockName = scheduleLockName;
  }

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

  public ThreadPoolProperties getExecuteJobCallbackPool() {
    return executeJobCallbackPool;
  }

  public void setExecuteJobCallbackPool(ThreadPoolProperties executeJobCallbackPool) {
    this.executeJobCallbackPool = executeJobCallbackPool;
  }

  public ThreadPoolProperties getCronJobTriggerPool() {
    return cronJobTriggerPool;
  }

  public void setCronJobTriggerPool(ThreadPoolProperties cronJobTriggerPool) {
    this.cronJobTriggerPool = cronJobTriggerPool;
  }
}
