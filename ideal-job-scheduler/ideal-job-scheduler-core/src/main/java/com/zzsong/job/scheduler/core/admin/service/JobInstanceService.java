package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.scheduler.core.admin.db.entity.JobInstanceDo;
import com.zzsong.job.scheduler.core.admin.db.repository.JobInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings("UnusedReturnValue")
@Service
public class JobInstanceService {
    private static final Logger log = LoggerFactory.getLogger(JobInstanceService.class);
    private static final int MAX_RESULT_LENGTH = 10000;
    private final JobInstanceRepository jobInstanceRepository;

    public JobInstanceService(JobInstanceRepository jobInstanceRepository) {
        this.jobInstanceRepository = jobInstanceRepository;
    }

    @Nonnull
    public JobInstanceDo saveInstance(@Nonnull JobInstanceDo instance) {
        String result = instance.getResult();
        if (result.length() > MAX_RESULT_LENGTH) {
            instance.setResult(result.substring(0, MAX_RESULT_LENGTH - 3) + "...");
        }
        return jobInstanceRepository.save(instance);
    }

    @Nullable
    public JobInstanceDo getJobInstance(long instanceId) {
        return jobInstanceRepository.findById(instanceId).orElse(null);
    }

    public void updateDispatchInfo(@Nonnull JobInstanceDo instance) {
        jobInstanceRepository.updateDispatchInfo(instance);
    }

    public int updateWhenTriggerCallback(@Nonnull JobInstanceDo instance) {
        return jobInstanceRepository.updateWhenTriggerCallback(instance);
    }

    public int deleteAllByCreatedTimeLessThan(LocalDateTime time) {
        return jobInstanceRepository.deleteAllByCreatedTimeLessThan(time);
    }

    public void flush() {
        jobInstanceRepository.flush();
    }
}
