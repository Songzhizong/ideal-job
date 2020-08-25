package cn.sh.ideal.job.executor.sample.spring.boot.job;

import cn.sh.ideal.job.executor.core.annotation.JobHandler;
import cn.sh.ideal.job.executor.core.annotation.JobHandlerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
@Component
@JobHandlerBean
public class SampleJob {
  private static final Logger log = LoggerFactory.getLogger(SampleJob.class);
  private static final AtomicLong atomicLong = new AtomicLong(0);

  static {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(() -> {
          log.info("count: {}", atomicLong.get());
        }, 10, 10, TimeUnit.SECONDS);
  }


  @JobHandler("demoJobHandler")
  public void demoJobHandler(@Nonnull String param) {
    atomicLong.incrementAndGet();
//    log.info("execute demoJobHandler, param = {}", param);
  }
}
