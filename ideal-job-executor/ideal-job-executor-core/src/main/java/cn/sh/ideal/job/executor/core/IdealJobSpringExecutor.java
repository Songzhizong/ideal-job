package cn.sh.ideal.job.executor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
public class IdealJobSpringExecutor extends JobExecutor
    implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
  private static final Logger log = LoggerFactory.getLogger(IdealJobSpringExecutor.class);

  @Override
  public void afterSingletonsInstantiated() {
    super.start();
  }

  @Override
  public void setApplicationContext(@Nonnull ApplicationContext applicationContext)
      throws BeansException {

  }

  @Override
  public void destroy() {
    super.destroy();
  }
}
