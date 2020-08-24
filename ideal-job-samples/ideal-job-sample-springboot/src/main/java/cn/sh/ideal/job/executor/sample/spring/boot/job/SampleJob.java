package cn.sh.ideal.job.executor.sample.spring.boot.job;

import cn.sh.ideal.job.executor.core.annotation.JobHandler;
import cn.sh.ideal.job.executor.core.annotation.JobHandlerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Component
@JobHandlerBean
public class SampleJob {
  private static final Logger log = LoggerFactory.getLogger(SampleJob.class);


  @JobHandler("demoJobHandler")
  public void demoJobHandler(@Nonnull String param) {
    log.info("execute demoJobHandler, param = {}", param);
  }
}
