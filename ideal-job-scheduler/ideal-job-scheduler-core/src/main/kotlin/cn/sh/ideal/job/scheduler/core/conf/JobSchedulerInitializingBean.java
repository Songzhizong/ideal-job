package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.scheduler.core.generator.IDGenerator;
import cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator;
import cn.sh.ideal.job.scheduler.core.trigger.ScheduleTrigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 宋志宗
 * @date 2020/7/4
 */
@Configuration
public class JobSchedulerInitializingBean implements InitializingBean {
  private final IDGenerator idGenerator;
  private final ScheduleTrigger scheduleTrigger;

  public JobSchedulerInitializingBean(IDGenerator idGenerator,
                                      ScheduleTrigger scheduleTrigger) {
    this.idGenerator = idGenerator;
    this.scheduleTrigger = scheduleTrigger;
  }

  @Override
  public void afterPropertiesSet() {
    JpaIdentityGenerator.setIdGenerator(idGenerator);
    scheduleTrigger.start();
  }
}
