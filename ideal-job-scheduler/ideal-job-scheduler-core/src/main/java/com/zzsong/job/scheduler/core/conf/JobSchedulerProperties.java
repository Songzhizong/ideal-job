package com.zzsong.job.scheduler.core.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
@Component
@ConfigurationProperties("ideal.job")
public class JobSchedulerProperties {
    /**
     * 任务实例保存天数
     */
    private int jobInstanceStorageDay = 30;
    /**
     * 建立连接后等待客户端注册的时间
     */
    private int weightRegisterSeconds = 10;
    @Nonnull
    private String accessToken = "";

    @NestedConfigurationProperty
    private ThreadPoolProperties executeJobCallbackPool = new ThreadPoolProperties();

    @NestedConfigurationProperty
    private ThreadPoolProperties cronJobTriggerPool = new ThreadPoolProperties();
}
