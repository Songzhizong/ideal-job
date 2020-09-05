package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.scheduler.api.dto.req.CreateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import com.zzsong.job.scheduler.core.admin.db.entity.JobExecutorDo;
import com.zzsong.job.scheduler.core.admin.db.repository.JobExecutorRepository;
import com.zzsong.job.scheduler.core.converter.ExecutorConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "UnusedReturnValue"})
@Service
public class JobExecutorService {
    private static final Logger log = LoggerFactory.getLogger(JobExecutorService.class);
    private static final String CACHE_NAME = "ideal:job:cache:executor";

    @Autowired
    private JobService jobService;
    private final JobExecutorRepository jobExecutorRepository;

    public JobExecutorService(JobExecutorRepository jobExecutorRepository) {
        this.jobExecutorRepository = jobExecutorRepository;
    }

    @Nonnull
    @CachePut(value = CACHE_NAME, key = "#result.executorId")
    public JobExecutorDo create(@Nonnull CreateExecutorArgs args) {
        String appName = args.getAppName();
        String title = args.getTitle();
        JobExecutorDo byAppName = jobExecutorRepository.findTopByAppName(appName);
        if (byAppName != null) {
            log.info("appName: {} 已存在", appName);
            throw new VisibleException("appName已存在");
        }
        JobExecutorDo jobExecutor = new JobExecutorDo();
        jobExecutor.setAppName(appName);
        jobExecutor.setTitle(title);
        jobExecutorRepository.save(jobExecutor);
        return jobExecutor;
    }

    @Nonnull
    @CachePut(value = CACHE_NAME, key = "#updateArgs.executorId")
    public JobExecutorDo update(@Nonnull UpdateExecutorArgs updateArgs) {
        Long executorId = updateArgs.getExecutorId();
        String appName = updateArgs.getAppName();
        String title = updateArgs.getTitle();
        JobExecutorDo byAppName = jobExecutorRepository.findTopByAppName(appName);
        if (byAppName != null && !byAppName.getExecutorId().equals(executorId)) {
            log.info("appName: {} 已被: {} 使用", appName, byAppName.getExecutorId());
            throw new VisibleException("appName已被使用");
        }
        JobExecutorDo executor = jobExecutorRepository.findById(executorId)
                .orElseThrow(() -> {
                    log.info("执行器: {} 不存在", executorId);
                    return new VisibleException("执行器不存在");
                });
        executor.setAppName(appName);
        executor.setTitle(title);
        jobExecutorRepository.save(executor);
        return executor;
    }

    @CacheEvict(value = CACHE_NAME, key = "#executorId")
    public void delete(long executorId) {
        boolean exists = jobService.existsByExecutorId(executorId);
        if (exists) {
            throw new VisibleException("该执行器存在定时任务");
        }
        JobExecutorDo jobExecutor = jobExecutorRepository
                .findById(executorId).orElse(null);
        if (jobExecutor != null) {
            jobExecutorRepository.delete(jobExecutor);
        } else {
            log.info("执行器: {} 不存在", executorId);
        }
    }

    @Nonnull
    public Res<List<ExecutorInfoRsp>> query(@Nonnull QueryExecutorArgs args,
                                            @Nonnull Paging paging) {
        String appName = args.getAppName();
        String title = args.getTitle();
        Page<JobExecutorDo> page = jobExecutorRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(appName)) {
                predicates.add(cb.like(root.get("appName"), appName + "%"));
            }
            if (StringUtils.isNotBlank(title)) {
                predicates.add(cb.like(root.get("title"), title + "%"));
            }
            return cq.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, SpringPages.paging2Pageable(paging));
        return SpringPages.toPageRes(page, ExecutorConverter::toExecutorInfoRsp);
    }

    @Nullable
    @Cacheable(value = CACHE_NAME, key = "#executorId")
    public JobExecutorDo loadById(long executorId) {
        return jobExecutorRepository.findById(executorId).orElse(null);
    }
}
