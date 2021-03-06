package com.zzsong.job.worker.annotation;

import java.lang.annotation.*;

/**
 * @author 宋志宗 on 2020/8/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandler {

  /**
   * job handler name
   */
  String value();
}
