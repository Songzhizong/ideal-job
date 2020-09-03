package cn.sh.ideal.job.scheduler.core.admin.repository;

import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings("UnusedReturnValue")
public interface JobInstanceRepository extends JpaRepository<JobInstance, Long> {

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update JobInstance ins " +
            "set ins.handleTime = :#{#instance.handleTime}, " +
            "    ins.finishedTime = :#{#instance.finishedTime}, " +
            "    ins.handleStatus = :#{#instance.handleStatus}, " +
            "    ins.result = :#{#instance.result}, " +
            "    ins.sequence = :#{#instance.sequence}, " +
            "    ins.updateTime = :#{#instance.updateTime} " +
            "where ins.instanceId = :#{#instance.instanceId} " +
            "    and ins.sequence < :#{#instance.sequence}")
    int updateWhenTriggerCallback(JobInstance instance);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update JobInstance ins " +
            "set ins.dispatchStatus = :#{#instance.dispatchStatus}, " +
            "    ins.dispatchMsg = :#{#instance.dispatchMsg}, " +
            "    ins.executorInstance = :#{#instance.executorInstance} " +
            "where ins.instanceId = :#{#instance.instanceId}")
    int updateDispatchInfo(JobInstance instance);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("delete from JobInstance where createdTime < :time")
    int deleteAllByCreatedTimeLessThan(LocalDateTime time);
}