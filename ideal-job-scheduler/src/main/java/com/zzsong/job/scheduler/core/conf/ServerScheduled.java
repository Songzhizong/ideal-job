package com.zzsong.job.scheduler.core.conf;

import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * @author 宋志宗 on 2020/8/30
 */
@Configuration
@EnableScheduling
public class ServerScheduled {
  private static final Logger log = LoggerFactory.getLogger(ServerScheduled.class);
  private final JobSchedulerProperties properties;
  private final JobInstanceService jobInstanceService;

  public ServerScheduled(JobSchedulerProperties properties,
                         JobInstanceService jobInstanceService) {
    this.properties = properties;
    this.jobInstanceService = jobInstanceService;
  }

  @Scheduled(cron = "37 40 5 * * ?")
  public void deleteJobInstanceScheduled() {
    int storageDay = properties.getJobInstanceStorageDay();
    if (storageDay < 1) {
      return;
    }
    LocalDateTime time = DateTimes.now().minusDays(storageDay);
    jobInstanceService.deleteAllByCreatedTimeLessThan(time)
        .doOnNext(count -> log.info("delete job instance started, remove count = {}", count))
        .subscribe();

  }
}
