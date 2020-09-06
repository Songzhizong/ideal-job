package com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository;

import com.zzsong.job.common.message.payload.TaskCallback;
import com.zzsong.job.scheduler.core.admin.storage.db.entity.JobInstanceDo;
import com.zzsong.job.scheduler.core.admin.storage.param.TaskResult;
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
      "set ins.handleTime = :#{#param.handleTime}, " +
      "    ins.finishedTime = :#{#param.finishedTime}, " +
      "    ins.handleStatus = :#{#param.handleStatus}, " +
      "    ins.result = :#{#param.result}, " +
      "    ins.sequence = :#{#param.sequence}, " +
      "    ins.updateTime = :#{#param.updateTime} " +
      "where ins.instanceId = :#{#param.instanceId} " +
      "    and ins.sequence < :#{#param.sequence}")
  int updateWhenTriggerCallback(@Param("param") TaskResult param);

  @Modifying
  @Transactional(rollbackFor = Exception.class)
  @Query("delete from JobInstanceDo where createdTime < :time")
  int deleteAllByCreatedTimeLessThan(@Param("time") LocalDateTime time);
}