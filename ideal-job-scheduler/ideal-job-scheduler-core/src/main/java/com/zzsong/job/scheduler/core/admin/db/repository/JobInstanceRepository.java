package com.zzsong.job.scheduler.core.admin.db.repository;

import com.zzsong.job.scheduler.core.admin.db.entity.JobInstanceDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings("UnusedReturnValue")
public interface JobInstanceRepository extends JpaRepository<JobInstanceDo, Long> {

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update JobInstanceDo ins " +
            "set ins.handleTime = :#{#instance.handleTime}, " +
            "    ins.finishedTime = :#{#instance.finishedTime}, " +
            "    ins.handleStatus = :#{#instance.handleStatus}, " +
            "    ins.result = :#{#instance.result}, " +
            "    ins.sequence = :#{#instance.sequence}, " +
            "    ins.updateTime = :#{#instance.updateTime} " +
            "where ins.instanceId = :#{#instance.instanceId} " +
            "    and ins.sequence < :#{#instance.sequence}")
    int updateWhenTriggerCallback(@Param("instance") JobInstanceDo instance);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update JobInstanceDo ins " +
            "set ins.dispatchStatus = :#{#instance.dispatchStatus}, " +
            "    ins.dispatchMsg = :#{#instance.dispatchMsg}, " +
            "    ins.executorInstance = :#{#instance.executorInstance} " +
            "where ins.instanceId = :#{#instance.instanceId}")
    int updateDispatchInfo(@Param("instance") JobInstanceDo instance);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("delete from JobInstanceDo where createdTime < :time")
    int deleteAllByCreatedTimeLessThan(@Param("time") LocalDateTime time);
}
