package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.common.loadbalancer.LbFactory;
import cn.sh.ideal.job.common.loadbalancer.SimpleLbFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Configuration
public class JobSchedulerBeanConfig {

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Bean
  public LbFactory lbFactory() {
    return new SimpleLbFactory();
  }
}
