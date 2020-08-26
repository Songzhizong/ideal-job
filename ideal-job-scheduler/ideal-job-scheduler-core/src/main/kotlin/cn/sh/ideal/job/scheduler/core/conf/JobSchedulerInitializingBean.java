package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.scheduler.core.generator.IDGenerator;
import cn.sh.ideal.job.scheduler.core.generator.JpaIdentityGenerator;
import cn.sh.ideal.job.scheduler.core.trigger.ScheduleTrigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author 宋志宗
 * @date 2020/7/4
 */
@Configuration
public class JobSchedulerInitializingBean implements InitializingBean {
  private final DataSource dataSource;
  private final IDGenerator idGenerator;
  private final JobSchedulerProperties properties;

  public JobSchedulerInitializingBean(DataSource dataSource,
                                      IDGenerator idGenerator,
                                      JobSchedulerProperties properties) {
    this.dataSource = dataSource;
    this.idGenerator = idGenerator;
    this.properties = properties;
  }

  @Override
  public void afterPropertiesSet() {
    ScheduleTrigger.INSTANCE.setDataSource(dataSource);
    ScheduleTrigger.INSTANCE.setLockTable(properties.getLockTable());
    ScheduleTrigger.INSTANCE.setScheduleLockName(properties.getScheduleLockName());
    JpaIdentityGenerator.setIdGenerator(idGenerator);
  }
}
