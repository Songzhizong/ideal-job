package cn.sh.ideal.job.executor.sample.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 宋志宗
 * @date 2020/7/14
 */
@RestController
@SpringBootApplication(scanBasePackages = "cn.sh.ideal")
public class ExecutorApplication {
  public static void main(String[] args) {
    // jpa生成表时指定 innodb 引擎
    System.setProperty("hibernate.dialect.storage_engine", "innodb");
    SpringApplication.run(ExecutorApplication.class, args);
  }

  @GetMapping("/test")
  public void test(String message) {

  }
}
