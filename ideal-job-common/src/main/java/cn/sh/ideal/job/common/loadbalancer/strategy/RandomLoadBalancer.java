package cn.sh.ideal.job.common.loadbalancer.strategy;

import cn.sh.ideal.job.common.loadbalancer.LbServer;
import cn.sh.ideal.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机策略
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class RandomLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {

    @Override
    @Nullable
    public Server chooseServer(@Nullable Object key,
                               @Nonnull List<Server> servers) {
        if (servers.isEmpty()) {
            return null;
        }
        int size = servers.size();
        if (size == 1) {
            return servers.get(0);
        }
        int random = ThreadLocalRandom.current().nextInt(size);
        return servers.get(random);
    }
}
