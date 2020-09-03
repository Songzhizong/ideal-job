package cn.sh.ideal.job.worker.annotation;

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
}
