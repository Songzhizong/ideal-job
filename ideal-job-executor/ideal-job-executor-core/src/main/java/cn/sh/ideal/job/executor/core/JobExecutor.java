package cn.sh.ideal.job.executor.core;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Getter
@Setter
public class JobExecutor {
  private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);
  private String accessToken;
  private int weight;
  private String schedulerAddresses;
  private String appName;
  private String ip;
  private int port;
  private int connectTimeOut = 200;
  private long writeTimeOut = 200;
  private long readTimeOut = 20000;

  private List<WebSocketRemoteExecutor> remoteExecutors = new ArrayList<>();


  public void start() {
    if (StringUtils.isBlank(schedulerAddresses)) {
      log.error("调度器地址为空.");
    }
    initRemoteExecutors();
  }

  private void initRemoteExecutors() {
    final String[] addresses = StringUtils.split(schedulerAddresses, ",");
    for (String address : addresses) {
      final WebSocketRemoteExecutor executor = new WebSocketRemoteExecutor();
      executor.setSchedulerAddress(address);
      executor.setAppName(appName);
      executor.setIp(ip);
      executor.setPort(port);
      executor.setWeight(weight);
      executor.setAccessToken(accessToken);
      executor.setConnectTimeOut(connectTimeOut);
      executor.setWriteTimeOut(writeTimeOut);
      executor.setReadTimeOut(readTimeOut);
      executor.startSocket();
      remoteExecutors.add(executor);
    }
  }

  public void destroy() {
    destroyRemoteExecutors();
  }

  private void destroyRemoteExecutors() {
    for (WebSocketRemoteExecutor executor : remoteExecutors) {
      executor.destroy();
    }
  }
}
