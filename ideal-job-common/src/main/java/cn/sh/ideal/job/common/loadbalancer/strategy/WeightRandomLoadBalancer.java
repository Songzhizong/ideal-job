package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机策略
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class WeightRandomLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {

    @Override
    @Nullable
    public Server chooseServer(@Nullable Object key,
                               @Nonnull List<Server> servers) {
        if (servers.isEmpty()) {
            return null;
        }
        final int size = servers.size();
        if (size == 1) {
            return servers.get(0);
        }
        // 为了保障随机均匀, 将权重放大一定的倍数
        final int multiple = 10;
        int sum = 0;
        for (LbServer server : servers) {
            final int weight = server.getWeight();
            if (weight < 1) {
                throw new IllegalArgumentException("Weight least for 1");
            }
            sum += weight * multiple;
        }
        if (sum == 0) {
            return null;
        }
        final int random = ThreadLocalRandom.current().nextInt(sum) + 1;
        int tmp = 0;
        for (Server server : servers) {
            final int weight = server.getWeight();
            if (weight < 1) {
                throw new IllegalArgumentException("Weight least for 1");
            }
            tmp += weight * multiple;
            if (tmp >= random) {
                return server;
            }
        }
        // 不可能运行到这里的, 如果有那肯定是电脑的问题
        String message = "加权随机算法运算错误: sum=" + sum + ", random=" + random + ", tmp=" + tmp;
        throw new WeightedRandomError(message);
    }

    public static class WeightedRandomError extends Error {
        public WeightedRandomError(String message) {
            super(message);
        }
    }
}
