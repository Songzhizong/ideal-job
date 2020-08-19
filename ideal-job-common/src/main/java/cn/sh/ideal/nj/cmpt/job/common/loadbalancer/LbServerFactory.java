package cn.sh.ideal.nj.cmpt.job.common.loadbalancer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/19
 */
public interface LbServerFactory {

  /**
   * 添加一组服务
   *
   * @param newServers 新加入的服务
   * @author 宋志宗
   * @date 2020/8/19 23:40
   */
  void addServers(@Nonnull List<LbServer> newServers);

  /**
   * 将服务标记为不可达
   *
   * @param server 不可达服务
   * @author 宋志宗
   * @date 2020/8/19 23:40
   */
  void markServerDown(@Nonnull LbServer server);

  /**
   * 获取所有可达的服务
   *
   * @return 所有可达的服务
   * @author 宋志宗
   * @date 2020/8/19 23:40
   */
  @Nonnull
  List<LbServer> getReachableServers();

  /**
   * 获取所有服务
   *
   * @return 所有已知的服务，包括可达的和不可达的。
   * @author 宋志宗
   * @date 2020/8/19 23:40
   */
  @Nonnull
  List<LbServer> getAllServers();
}
