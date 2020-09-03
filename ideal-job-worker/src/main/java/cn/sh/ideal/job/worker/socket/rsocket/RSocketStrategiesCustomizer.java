package cn.sh.ideal.job.worker.socket.rsocket;

import org.springframework.messaging.rsocket.RSocketStrategies;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@FunctionalInterface
public interface RSocketStrategiesCustomizer {
    void customize(RSocketStrategies.Builder strategies);
}
