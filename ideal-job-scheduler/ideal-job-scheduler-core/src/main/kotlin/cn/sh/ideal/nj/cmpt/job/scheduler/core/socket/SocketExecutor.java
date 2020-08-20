package cn.sh.ideal.nj.cmpt.job.scheduler.core.socket;

import cn.sh.ideal.nj.cmpt.job.common.executor.Executor;
import cn.sh.ideal.nj.cmpt.job.common.pojo.payload.ExecuteParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.websocket.Session;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class SocketExecutor implements Executor {
  @Nonnull
  private final String appName;
  @Nonnull
  private final String instanceId;
  @Nonnull
  private final Session session;

  private int weight = 1;

  public SocketExecutor(@Nonnull String appName,
                        @Nonnull String instanceId,
                        @Nonnull Session session) {
    this.appName = appName;
    this.instanceId = instanceId;
    this.session = session;
  }

  @Nonnull
  public String getAppName() {
    return appName;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  /**
   * 处理socket异常
   *
   * @param throwable 异常信息
   * @author 宋志宗
   * @date 2020/8/20 6:26 下午
   */
  public void disposeSocketError(Throwable throwable) {

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
  public boolean availableBeat() {
    if (!session.isOpen()) {
      return false;
    }
    return false;
  }

  @Override
  public int idleBeat(@Nullable Object key) {
    return 0;
  }

  @Override
  public int getWeight() {
    return weight;
  }
}
