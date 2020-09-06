package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.scheduler.core.admin.storage.JobInstanceStorage;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
import com.zzsong.job.scheduler.core.pojo.JobInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings("UnusedReturnValue")
@Service
public class JobInstanceService {
  private static final Logger log = LoggerFactory.getLogger(JobInstanceService.class);
  private static final int MAX_RESULT_LENGTH = 10000;
  private final JobInstanceStorage jobInstanceStorage;

  public JobInstanceService(JobInstanceStorage jobInstanceStorage) {
    this.jobInstanceStorage = jobInstanceStorage;
  }

  @Nonnull
  public Mono<JobInstance> saveInstance(@Nonnull JobInstance instance) {
    String result = instance.getResult();
    if (result.length() > MAX_RESULT_LENGTH) {
      instance.setResult(result.substring(0, MAX_RESULT_LENGTH - 3) + "...");
    }
    return jobInstanceStorage.save(instance);
  }

  @Nullable
  public Mono<Optional<JobInstance>> getJobInstance(long instanceId) {
    return jobInstanceStorage.findById(instanceId);
  }

  public Mono<Integer> updateByTaskResult(@Nonnull TaskResult param) {
    return jobInstanceStorage.updateByTaskResult(param);
  }

  public Mono<Integer> deleteAllByCreatedTimeLessThan(LocalDateTime time) {
    return jobInstanceStorage.deleteAllByCreatedTimeLessThan(time);
  }
}
