package cn.sh.ideal.job.executor.core;

import cn.sh.ideal.job.executor.core.annotation.JobHandler;
import cn.sh.ideal.job.executor.core.annotation.JobHandlerBean;
import cn.sh.ideal.job.executor.core.handler.JobHandlerFactory;
import cn.sh.ideal.job.executor.core.handler.impl.MethodJobHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
public final class IdealJobSpringExecutor extends JobExecutor
    implements ApplicationContextAware, SmartInitializingSingleton {
  private static final Logger log = LoggerFactory.getLogger(IdealJobSpringExecutor.class);
  private static ApplicationContext applicationContext;

  public IdealJobSpringExecutor() {
    super();
  }

  @Override
  public void afterSingletonsInstantiated() {
    initJobHandler();
    super.start();
  }

  @Override
  public void setApplicationContext(@Nonnull ApplicationContext applicationContext)
      throws BeansException {
    IdealJobSpringExecutor.applicationContext = applicationContext;
  }

  private void initJobHandler() {
    Map<String, Object> beanMapping = applicationContext
        .getBeansWithAnnotation(JobHandlerBean.class);
    Collection<Object> beans = beanMapping.values();
    for (Object bean : beans) {
      Class<?> aClass = bean.getClass();
      Method[] methods = aClass.getMethods();
      for (Method method : methods) {
        JobHandler annotation = method.getAnnotation(JobHandler.class);
        if (annotation != null) {
          String handlerName = annotation.value();
          String initMethodName = annotation.initMethod();
          String destroyMethodName = annotation.destroyMethod();
          if (StringUtils.isBlank(handlerName)) {
            String className = bean.getClass().getName();
            String methodName = method.getName();
            log.error("{}#{} 未指定handlerName", className, methodName);
            continue;
          }
          Class<?>[] parameterTypes = method.getParameterTypes();
          if (parameterTypes.length > 1) {
            String className = bean.getClass().getName();
            String methodName = method.getName();
            log.error("{}#{} 参数列表过长, 支持空入参或者一个String类型入参", className, methodName);
            continue;
          }
          boolean hasParam;
          if (parameterTypes.length == 0) {
            hasParam = false;
          } else {
            Class<?> firstType = parameterTypes[0];
            if (!firstType.isAssignableFrom(String.class)) {
              String className = bean.getClass().getName();
              String methodName = method.getName();
              log.error("{}#{} 参数类型不合法, 支持空入参或者一个String类型入参", className, methodName);
              continue;
            }
            hasParam = true;
          }
          method.setAccessible(true);

          Method initMethod = null;
          Method destroyMethod = null;
          if (StringUtils.isNotBlank(initMethodName)) {
            try {
              initMethod = bean.getClass().getDeclaredMethod(initMethodName);
              initMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
              String className = bean.getClass().getName();
              log.error("Class: {} 下找不到名称为: {} 的方法", className, initMethodName);
            }
          }
          if (StringUtils.isNotBlank(destroyMethodName)) {
            try {
              destroyMethod = bean.getClass().getDeclaredMethod(destroyMethodName);
              destroyMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
              String className = bean.getClass().getName();
              log.error("Class: {} 下找不到名称为: {} 的方法", className, destroyMethodName);
            }
          }
          MethodJobHandler jobHandler = new MethodJobHandler(bean, method, hasParam, initMethod, destroyMethod);
          JobHandlerFactory.register(handlerName, jobHandler);
        }
      }
    }
  }
}
