package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 加权随机策略
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class WeightedRandomLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {

  @Override
  @Nullable
  public Server chooseServer(@Nullable Object key,
                             @Nonnull List<Server> reachableServers) {
    if (reachableServers.isEmpty()) {
      return null;
    }
    int size = reachableServers.size();
    if (size == 1) {
      return reachableServers.get(0);
    }
    // 为了保障随机均匀, 将权重放大一定的倍数
    final int multiple = 10;
    int sum = 0;
    for (LbServer server : reachableServers) {
      sum += server.checkAndGetWeight() * multiple;
    }
    if (sum == 0) {
      return null;
    }
    int random = ThreadLocalRandom.current().nextInt(1, sum + 1);
    int tmp = 0;
    Server selected = null;
    for (Server server : reachableServers) {
      final int weight = server.checkAndGetWeight();
      tmp += weight * multiple;
      if (tmp >= random) {
        selected = server;
        break;
      }
    }
    return selected;
  }
}
