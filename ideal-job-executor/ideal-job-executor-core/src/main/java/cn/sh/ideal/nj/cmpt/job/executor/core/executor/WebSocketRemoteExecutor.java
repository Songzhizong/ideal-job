package cn.sh.ideal.nj.cmpt.job.executor.core.executor;

import cn.sh.ideal.nj.cmpt.job.common.executor.RemoteExecutor;
import cn.sh.ideal.nj.cmpt.job.common.pojo.payload.ExecuteCallbackParam;
import cn.sh.ideal.nj.cmpt.job.common.pojo.payload.ExecuteParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class WebSocketRemoteExecutor implements RemoteExecutor {
  /**
   * 调度器程序地址
   */
  private String schedulerAddress;
  /**
   * 应用名称
   */
  private String appName;
  /**
   * 调度器ip地址
   */
  private String ip;
  /**
   * 调度器端口号
   */
  private int port;

  public String getSchedulerAddress() {
    return schedulerAddress;
  }

  public void setSchedulerAddress(String schedulerAddress) {
    this.schedulerAddress = schedulerAddress;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public void callback(ExecuteCallbackParam param) {

  }

  @Override
  public boolean execute(ExecuteParam param) {
    return false;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return ip + ":" + port;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public boolean availableBeat() {
    return false;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public int getWeight() {
    return 1;
  }
}
