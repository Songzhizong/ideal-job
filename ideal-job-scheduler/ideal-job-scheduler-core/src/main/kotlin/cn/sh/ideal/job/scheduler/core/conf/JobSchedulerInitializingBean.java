package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.scheduler.core.generator.IDGenerator;
import cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 宋志宗
 * @date 2020/7/4
 */
@Configuration
public class JobSchedulerInitializingBean implements InitializingBean {
  private final IDGenerator idGenerator;

  public JobSchedulerInitializingBean(IDGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  @Override
  public void afterPropertiesSet() {
    JpaIdentityGenerator.setIdGenerator(idGenerator);
  }
}
