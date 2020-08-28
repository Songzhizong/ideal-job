package cn.sh.ideal.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/8/19
 */
public interface LoadBalancer<Server extends LbServer> {
  /**
   * 选取一个server
   *
   * @param key     负载均衡器可以使用该对象来确定返回哪个服务
   * @param servers 可达服务列表
   * @return 选取的服务
   * @author 宋志宗
   * @date 2020/8/28 11:19 上午
   */
  @Nullable
  Server chooseServer(@Nullable Object key,
                      @Nonnull List<Server> servers);
}
