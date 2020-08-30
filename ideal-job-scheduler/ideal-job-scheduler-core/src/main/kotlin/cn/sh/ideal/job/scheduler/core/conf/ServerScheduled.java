package cn.sh.ideal.job.scheduler.core.conf;

import cn.sh.ideal.job.common.utils.DateTimes;
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * @author 宋志宗
 * @date 2020/8/30
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
      storageDay = 30;
    }
    LocalDateTime time = DateTimes.now().minusDays(storageDay);
    int count = jobInstanceService.deleteAllByCreatedTimeLessThan(time);
    log.info("delete job instance started, remove count = {}", count);
  }
}
