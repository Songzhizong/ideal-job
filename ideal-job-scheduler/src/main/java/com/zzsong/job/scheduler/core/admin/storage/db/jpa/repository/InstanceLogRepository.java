package com.zzsong.job.scheduler.core.admin.storage.db.jpa.repository;

import com.zzsong.job.scheduler.core.admin.storage.db.entity.InstanceLogDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/15
 */
public interface InstanceLogRepository extends JpaRepository<InstanceLogDo, Long> {

  @Nonnull
  List<InstanceLogDo> findAllByInstanceId(long instanceId);

  @Modifying

  @Transactional(rollbackFor = Exception.class)
  @Query("delete from InstanceLogDo where logTime < :time")
  int deleteAllByLogTimeLessThan(@Param("time") long time);
}
