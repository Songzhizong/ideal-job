package com.zzsong.job.scheduler.core.admin.repository.impl;

import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.scheduler.core.admin.entity.vo.DispatchJobView;
import com.zzsong.job.scheduler.core.admin.repository.JobInfoRepositoryCustom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@Repository
public class JobInfoRepositoryCustomImpl implements JobInfoRepositoryCustom {
    private final JdbcTemplate jdbcTemplate;

    public JobInfoRepositoryCustomImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void batchUpdateTriggerInfo(Collection<DispatchJobView> jobInfos) {
        final String sql = "update ideal_job_info" +
                " set job_status = ?," +
                " last_trigger_time = ?," +
                " next_trigger_time = ?," +
                " update_time = ?" +
                " where job_id = ?";
        final LocalDateTime now = DateTimes.now();
        List<Object[]> list = jobInfos.stream()
                .map(v -> new Object[]{v.getJobStatus(), v.getLastTriggerTime(),
                        v.getNextTriggerTime(), now, v.getJobId()})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, list);

    }
}
