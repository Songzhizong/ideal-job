package com.zzsong.job.sample.spring.boot;

import com.zzsong.job.worker.annotation.JobHandler;
import com.zzsong.job.worker.annotation.JobHandlerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/8/21
 */
@Component
@JobHandlerBean
public class SampleJob {
  private static final Logger log = LoggerFactory.getLogger(SampleJob.class);

  @JobHandler("demoJobHandler")
  public String demoJobHandler(@Nonnull String param) throws InterruptedException {
    log.info("execute demoJobHandler, param = {}", param);
    TimeUnit.SECONDS.sleep(5);
    return "demoJobHandler 接收到任务并已执行完成";
  }
}
