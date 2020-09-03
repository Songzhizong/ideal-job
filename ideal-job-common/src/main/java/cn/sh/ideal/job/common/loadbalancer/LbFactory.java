package cn.sh.ideal.job.common.loadbalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * 负载均衡器工厂
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface LbFactory<Server extends LbServer> {

    /**
     * 选取一个server, 默认为轮询策略
     *
     * @param serverName 服务名称
     * @param key        负载均衡器可以使用该对象来确定返回哪个服务
     * @author 宋志宗
     * @date 2020/8/20 9:59 上午
     */
    @Nullable
    default Server chooseServer(@Nonnull String serverName,
                                @Nullable Object key) {
        return chooseServer(serverName, LbStrategyEnum.ROUND_ROBIN, key);
    }

    /**
     * 选取一个server
     *
     * @param serverName 服务名称
     * @param strategy   负载均衡策略
     * @param key        负载均衡器可以使用该对象来确定返回哪个服务
     * @author 宋志宗
     * @date 2020/8/20 9:59 上午
     */
    @Nullable
    Server chooseServer(@Nonnull String serverName,
                        @Nonnull LbStrategyEnum strategy,
                        @Nullable Object key);

    /**
     * 获取负载均衡器, 默认为轮询策略
     *
     * @param serverName 服务名称
     * @return LoadBalancer
     * @author 宋志宗
     * @date 2020/8/20 9:50 上午
     */
    @Nonnull
    default LoadBalancer<Server> getLoadBalancer(@Nonnull String serverName) {
        return getLoadBalancer(serverName, LbStrategyEnum.ROUND_ROBIN);
    }

    /**
     * 获取负载均衡器
     *
     * @param serverName 服务名称
     * @param strategy   负载均衡策略
     * @return LoadBalancer
     * @author 宋志宗
     * @date 2020/8/20 9:50 上午
     */
    @Nonnull
    LoadBalancer<Server> getLoadBalancer(@Nonnull String serverName,
                                         @Nonnull LbStrategyEnum strategy);


    /**
     * 获取服务持有者, 如果不存在会自动创建一个新的
     *
     * @param serverName 服务名称
     * @return LbServerFactory
     * @author 宋志宗
     * @date 2020/8/20 10:27 上午
     */
    @Nonnull
    default LbServerHolder<Server> getServerHolder(@Nonnull String serverName) {
        return getServerHolder(serverName, null);
    }

    /**
     * 获取服务持有者, 如果不存在则会使用指定值
     *
     * @param serverName 服务名称
     * @return LbServerFactory
     * @author 宋志宗
     * @date 2020/8/20 10:27 上午
     */
    @Nonnull
    LbServerHolder<Server> getServerHolder(
            @Nonnull String serverName,
            @Nullable Function<String, LbServerHolder<Server>> function);

    /**
     * 销毁对象
     */
    void destroy();
}
