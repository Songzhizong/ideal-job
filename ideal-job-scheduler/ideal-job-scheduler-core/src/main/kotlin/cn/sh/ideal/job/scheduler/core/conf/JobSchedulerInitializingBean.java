package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.scheduler.core.generator.IDGenerator;
import cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator;
import cn.sh.ideal.job.scheduler.core.trigger.TimingSchedule;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 宋志宗
 * @date 2020/7/4
 */
@Configuration
public class JobSchedulerInitializingBean implements InitializingBean, DisposableBean {
  private final IDGenerator idGenerator;
  private final TimingSchedule timingSchedule;

  public JobSchedulerInitializingBean(IDGenerator idGenerator,
                                      TimingSchedule timingSchedule) {
    this.idGenerator = idGenerator;
    this.timingSchedule = timingSchedule;
  }

  @Override
  public void afterPropertiesSet() {
    JpaIdentityGenerator.setIdGenerator(idGenerator);
    timingSchedule.start();
  }

  @Override
  public void destroy() {
    timingSchedule.stop();
  }
}
