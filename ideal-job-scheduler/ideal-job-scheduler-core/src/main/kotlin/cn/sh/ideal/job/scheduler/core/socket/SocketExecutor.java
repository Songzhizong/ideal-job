package cn.sh.ideal.job.scheduler.core.socket;

import cn.sh.ideal.job.common.executor.Executor;
import cn.sh.ideal.job.common.pojo.HeartbeatMessage;
import cn.sh.ideal.job.common.pojo.payload.ExecuteParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SocketExecutor implements Executor {
  private static final Logger log = LoggerFactory.getLogger(SocketExecutor.class);
  private final long createTime = System.currentTimeMillis();
  @Nonnull
  private final String appName;
  @Nonnull
  private final String instanceId;
  @Nonnull
  private final Session session;

  private int weight = 1;
  private volatile int weightRegisterSeconds = 60;
  private volatile boolean registered = false;
  private volatile boolean destroyed = false;

  public SocketExecutor(@Nonnull String appName,
                        @Nonnull String instanceId,
                        @Nonnull Session session) {
    this.appName = appName;
    this.instanceId = instanceId;
    this.session = session;
    new Thread(() -> {
      while (!registered && !destroyed) {
        if (System.currentTimeMillis() - createTime > weightRegisterSeconds * 1000) {
          this.destroy();
          log.info("SocketExecutor 超过 {}秒未注册, 已销毁, appName: {}, instanceId: {}",
              weightRegisterSeconds, appName, instanceId);
          break;
        } else {
          try {
            TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }).start();
  }

  @Nonnull
  public String getAppName() {
    return appName;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  @SuppressWarnings("unused")
  public int getWeightRegisterSeconds() {
    return weightRegisterSeconds;
  }

  public void setWeightRegisterSeconds(int weightRegisterSeconds) {
    this.weightRegisterSeconds = weightRegisterSeconds;
  }

  @SuppressWarnings("unused")
  public boolean isRegistered() {
    return registered;
  }

  public void setRegistered(boolean registered) {
    this.registered = registered;
  }

  /**
   * 处理socket异常
   *
   * @param throwable 异常信息
   * @author 宋志宗
   * @date 2020/8/20 6:26 下午
   */
  public void disposeSocketError(@Nonnull Throwable throwable) {
    log.info("socket error: {}", throwable.getClass().getSimpleName() + ":" + throwable.getMessage());
  }

  /**
   * 执行任务
   *
   * @param param 触发器参数
   * @return 执行结果
   * @author 宋志宗
   * @date 2020/8/20 6:26 下午
   */
  @Override
  public boolean execute(ExecuteParam param) {
    return false;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public boolean heartbeat() {
    if (!session.isOpen() || destroyed) {
      return false;
    }
    try {
      session.getBasicRemote().sendText(HeartbeatMessage.INSTANCE.jsonString());
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public int getWeight() {
    return weight;
  }

  @Override
  public void destroy() {
    synchronized (this) {
      if (!destroyed) {
        try {
          session.close();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          log.info("Destroy SocketExecutor, appName: {}, instanceId: {}, sessionId: {}",
              appName, instanceId, session.getId());
          destroyed = true;
        }
      }
    }
  }
}
