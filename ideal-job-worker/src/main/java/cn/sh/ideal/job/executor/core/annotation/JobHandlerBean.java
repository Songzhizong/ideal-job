package cn.sh.ideal.job.executor.core.annotation;

import java.lang.annotation.*;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandlerBean {
}
