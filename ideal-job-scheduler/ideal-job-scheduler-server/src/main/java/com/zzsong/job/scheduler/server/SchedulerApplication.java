package com.zzsong.job.scheduler.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 宋志宗 on 2020/7/14
 */
@EnableScheduling
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.zzsong.job.scheduler.core"})
@EntityScan(basePackages = {"com.zzsong.job.scheduler.core"})
@SpringBootApplication(scanBasePackages = "com.zzsong.job")
public class SchedulerApplication {
  public static void main(String[] args) {
    // jpa生成表时指定 innodb 引擎
    System.setProperty("hibernate.dialect.storage_engine", "innodb");
    SpringApplication.run(SchedulerApplication.class, args);
  }
}
