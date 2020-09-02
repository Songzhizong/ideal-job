package cn.sh.ideal.job.scheduler.core.socket.rsocket;

import cn.sh.ideal.job.common.message.payload.TaskParam;
import cn.sh.ideal.job.common.worker.TaskWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.rsocket.RSocketRequester;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/9/1
 */
public class RSocketTaskWorker implements TaskWorker {
  private static final Logger log = LoggerFactory.getLogger(RSocketTaskWorker.class);
  @Nonnull
  private final String appName;
  @Nonnull
  private final String instanceId;
  @Nonnull
  private final RSocketRequester requester;

  private int weight = 1;

  public RSocketTaskWorker(@Nonnull String appName,
                           @Nonnull String instanceId,
                           @Nonnull RSocketRequester requester) {
    this.appName = appName;
    this.instanceId = instanceId;
    this.requester = requester;
  }

  @Override
  public void execute(@Nonnull TaskParam param) {

  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return null;
  }

  @Override
  public boolean heartbeat() {
    return false;
  }

  @Override
  public void destroy() {
    requester.rsocket().dispose();
    log.info("{} -> {} destroy.", appName, instanceId);
  }
}
