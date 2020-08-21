package cn.sh.ideal.job.executor.core;

import java.lang.annotation.*;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandler {

  /**
   * job handler name
   */
  String value();

  /**
   * init handler, invoked when JobThread init
   */
  String init() default "";

  /**
   * destroy handler, invoked when JobThread destroy
   */
  String destroy() default "";
}
