package cn.sh.ideal.job.scheduler.core.admin.service;

import cn.sh.ideal.job.common.exception.VisibleException;
import cn.sh.ideal.job.common.transfer.Paging;
import cn.sh.ideal.job.common.transfer.Res;
import cn.sh.ideal.job.common.transfer.SpringPages;
import cn.sh.ideal.job.scheduler.api.dto.req.CreateExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.QueryExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateExecutorArgs;
import cn.sh.ideal.job.scheduler.api.dto.rsp.ExecutorInfoRsp;
import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor;
import cn.sh.ideal.job.scheduler.core.admin.repository.JobExecutorRepository;
import cn.sh.ideal.job.scheduler.core.converter.ExecutorConverter;
import kotlin.collections.ArrayDeque;
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
    public JobExecutor create(@Nonnull CreateExecutorArgs args) {
        String appName = args.getAppName();
        String title = args.getTitle();
        JobExecutor byAppName = jobExecutorRepository.findTopByAppName(appName);
        if (byAppName != null) {
            throw new VisibleException("appName已存在");
        }
        JobExecutor jobExecutor = new JobExecutor();
        jobExecutor.setAppName(appName);
        jobExecutor.setTitle(title);
        jobExecutorRepository.save(jobExecutor);
        return jobExecutor;
    }

    @Nonnull
    @CachePut(value = CACHE_NAME, key = "#updateArgs.executorId")
    public JobExecutor update(@Nonnull UpdateExecutorArgs updateArgs) {
        Long executorId = updateArgs.getExecutorId();
        String appName = updateArgs.getAppName();
        String title = updateArgs.getTitle();
        JobExecutor byAppName = jobExecutorRepository.findTopByAppName(appName);
        if (byAppName != null && !byAppName.getExecutorId().equals(executorId)) {
            log.info("appName: {} 已被: {} 使用", appName, byAppName.getExecutorId());
            throw new VisibleException("appName已被使用");
        }
        JobExecutor executor = jobExecutorRepository.findById(executorId)
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
        JobExecutor jobExecutor = jobExecutorRepository
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
        Page<JobExecutor> page = jobExecutorRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayDeque<>();
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
    public JobExecutor loadById(long executorId) {
        return jobExecutorRepository.findById(executorId).orElse(null);
    }
}
