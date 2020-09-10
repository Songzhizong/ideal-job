package com.zzsong.job.worker.annotation;

import java.lang.annotation.*;

/**
 * @author 宋志宗 on 2020/8/22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandlerBean {
}
