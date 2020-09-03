package com.zzsong.job.common.loadbalancer.strategy;

import com.zzsong.job.common.loadbalancer.LbServer;
import com.zzsong.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 忙碌转移,选择一个空闲度最高的服务返回
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class BusyTransferLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {

    public BusyTransferLoadBalancer() {
    }

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

        Server selected = null;
        Integer minIdleLevel = null;
        for (Server server : servers) {
            int idleLevel = server.idleBeat(key);
            if (idleLevel < 1) {
                return server;
            }
            if (minIdleLevel == null || idleLevel < minIdleLevel) {
                minIdleLevel = idleLevel;
                selected = server;
            }
        }
        return selected;
    }
}
