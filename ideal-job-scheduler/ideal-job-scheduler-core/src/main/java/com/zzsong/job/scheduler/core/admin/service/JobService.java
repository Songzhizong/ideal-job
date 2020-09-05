package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.SpringPages;
import com.zzsong.job.scheduler.api.dto.req.CreateJobArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryJobArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateJobArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobInfoRsp;
import com.zzsong.job.scheduler.api.pojo.JobView;
import com.zzsong.job.scheduler.api.pojo.JobWorker;
import com.zzsong.job.scheduler.core.admin.db.entity.JobInfoDo;
import com.zzsong.job.scheduler.core.admin.db.repository.JobInfoRepository;
import com.zzsong.job.scheduler.core.converter.JobInfoConverter;
import com.zzsong.job.scheduler.core.dispatch.JobDispatch;
import com.zzsong.job.scheduler.core.utils.CronExpression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗
 * @date 2020/9/2
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private static final long PRE_READ_MS = 5000L;

    @Autowired
    private JobDispatch jobDispatch;
    @Autowired
    private JobExecutorService jobExecutorService;

    private final JobInfoRepository jobInfoRepository;

    public JobService(JobInfoRepository jobInfoRepository) {
        this.jobInfoRepository = jobInfoRepository;
    }

    /**
     * 新建任务
     *
     * @param createJobArgs 新增任务请求参数
     * @return 任务id
     * @author 宋志宗
     * @date 2020/8/26 7:36 下午
     */
    public long createJob(@Nonnull CreateJobArgs createJobArgs) {
        long executorId = createJobArgs.getExecutorId();
        boolean autoStart = createJobArgs.isAutoStart();
        String cron = createJobArgs.getCron();
        Optional<JobWorker> block = jobExecutorService.loadById(executorId).block();
        if (block == null || !block.isPresent()) {
            log.info("新建任务失败, 执行器: {} 不存在", executorId);
            throw new VisibleException(CommonResMsg.NOT_FOUND, "执行器不存在");
        }
        JobInfoDo jobInfo = JobInfoConverter.fromCreateJobArgs(createJobArgs);
        if (StringUtils.isNotBlank(cron)) {
            boolean validExpression = CronExpression.isValidExpression(cron);
            if (!validExpression) {
                throw new VisibleException(CommonResMsg.BAD_REQUEST, "cron表达式不合法");
            }
            if (autoStart) {
                long nextTriggerTime = getNextTriggerTime(cron);
                jobInfo.setJobStatus(JobInfoDo.JOB_START);
                jobInfo.setNextTriggerTime(nextTriggerTime);
            }
        }
        jobInfoRepository.save(jobInfo);
        return jobInfo.getJobId();
    }

    /**
     * 更新任务信息
     *
     * @param args 更新参数
     * @author 宋志宗
     * @date 2020/8/26 8:48 下午
     */
    public void updateJob(@Nonnull UpdateJobArgs args) {
        long jobId = args.getJobId();
        String cron = args.getCron();
        JobInfoDo jobInfo = jobInfoRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.info("任务: {} 不存在", jobId);
                    return new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在");
                });
        BeanUtils.copyProperties(args, jobInfo);
        if (StringUtils.isNotBlank(cron)) {
            boolean validExpression = CronExpression.isValidExpression(cron);
            if (!validExpression) {
                throw new VisibleException(CommonResMsg.BAD_REQUEST, "cron表达式不合法");
            }
            if (jobInfo.getJobStatus() == JobInfoDo.JOB_START) {
                long nextTriggerTime = getNextTriggerTime(cron);
                jobInfo.setNextTriggerTime(nextTriggerTime);
            }
        } else {
            jobInfo.setJobStatus(JobInfoDo.JOB_STOP);
        }
        jobInfoRepository.save(jobInfo);
    }

    /**
     * 移除任务
     *
     * @param jobId 任务id
     * @author 宋志宗
     * @date 2020/8/26 8:49 下午
     */
    public void removeJob(long jobId) {
        JobInfoDo jobInfo = jobInfoRepository.findById(jobId).orElse(null);
        if (jobInfo == null) {
            log.info("任务: {} 不存在", jobId);
            return;
        }
        jobInfoRepository.delete(jobInfo);
    }

    /**
     * 查询任务信息
     *
     * @param args   查询参数
     * @param paging 分页参数
     * @return 任务信息列表
     * @author 宋志宗
     * @date 2020/8/26 8:51 下午
     */
    @Nonnull
    public Res<List<JobInfoRsp>> query(@Nonnull QueryJobArgs args, @Nonnull Paging paging) {
        Long executorId = args.getExecutorId();
        String jobName = args.getJobName();
        String executorHandler = args.getExecutorHandler();
        Integer jobStatus = args.getJobStatus();
        String application = args.getApplication();
        String tenantId = args.getTenantId();
        String bizType = args.getBizType();
        String customTag = args.getCustomTag();
        String businessId = args.getBusinessId();

        Page<JobInfoDo> page = jobInfoRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (executorId != null) {
                predicates.add(cb.equal(root.get("executorId"), executorId));
            }
            if (StringUtils.isNotBlank(jobName)) {
                predicates.add(cb.like(root.get("jobName"), jobName + "%"));
            }
            if (StringUtils.isNotBlank(executorHandler)) {
                predicates.add(cb.like(root.get("executorHandler"), executorHandler + "%"));
            }
            if (jobStatus != null) {
                predicates.add(cb.equal(root.get("jobStatus"), jobStatus));
            }
            if (StringUtils.isNotBlank(application)) {
                predicates.add(cb.equal(root.get("application"), application));
            }
            if (StringUtils.isNotBlank(tenantId)) {
                predicates.add(cb.equal(root.get("tenantId"), tenantId));
            }
            if (StringUtils.isNotBlank(bizType)) {
                predicates.add(cb.equal(root.get("bizType"), bizType));
            }
            if (StringUtils.isNotBlank(customTag)) {
                predicates.add(cb.equal(root.get("customTag"), customTag));
            }
            if (StringUtils.isNotBlank(businessId)) {
                predicates.add(cb.equal(root.get("businessId"), businessId));
            }
            return cq.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, SpringPages.paging2Pageable(paging));
        return SpringPages.toPageRes(page, JobInfoConverter::toJobInfoRsp);
    }

    /**
     * 启用任务
     *
     * @param jobId 任务id
     * @author 宋志宗
     * @date 2020/8/20 4:38 下午
     */
    public void enableJob(long jobId) {
        JobInfoDo jobInfo = jobInfoRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.info("任务: {} 不存在", jobId);
                    return new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在");
                });
        if (jobInfo.getJobStatus() == JobInfoDo.JOB_START) {
            log.info("任务: {} 正在在运行中", jobId);
            return;
        }
        String cron = jobInfo.getCron();
        if (StringUtils.isBlank(cron)) {
            log.info("启动任务: {} 失败, cron表达式为空", jobId);
            throw new VisibleException(CommonResMsg.BAD_REQUEST, "cron表达式为空");
        }
        long nextTriggerTime = getNextTriggerTime(cron);
        jobInfo.setJobStatus(JobInfoDo.JOB_START);
        jobInfo.setLastTriggerTime(0);
        jobInfo.setNextTriggerTime(nextTriggerTime);
        jobInfoRepository.save(jobInfo);
    }

    /**
     * 停用任务
     *
     * @param jobId 任务id
     * @author 宋志宗
     * @date 2020/8/20 4:38 下午
     */
    public void disableJob(long jobId) {
        JobInfoDo jobInfo = jobInfoRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.info("任务: {} 不存在", jobId);
                    return new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在");
                });
        if (jobInfo.getJobStatus() == JobInfoDo.JOB_STOP) {
            log.info("任务: {} 为停止状态", jobId);
            return;
        }
        jobInfo.setJobStatus(JobInfoDo.JOB_STOP);
        jobInfo.setLastTriggerTime(0);
        jobInfo.setNextTriggerTime(0);
        jobInfoRepository.save(jobInfo);
    }

    public void triggerJob(long jobId, @Nullable String customExecuteParam) {
        JobView dispatchJobView = jobInfoRepository.findDispatchJobViewById(jobId);
        if (dispatchJobView == null) {
            log.info("任务: {} 不存在", jobId);
            throw new VisibleException(CommonResMsg.NOT_FOUND, "任务不存在");
        }
        jobDispatch.dispatch(dispatchJobView, TriggerTypeEnum.MANUAL, customExecuteParam);
    }

    /**
     * 读取一批待执行的任务
     *
     * @param maxNextTime 最大下次执行时间
     * @param count       读取数量
     * @return 待执行任务列表
     * @date 2020/8/24 8:46 下午
     */
    public List<JobView> loadScheduleJobViews(long maxNextTime, int count) {
        Sort sort = Sort.by("jobId").ascending();
        PageRequest pageRequest = PageRequest.of(0, count, sort);
        int jobStart = JobInfoDo.JOB_START;
        return jobInfoRepository.loadScheduleJobViews(jobStart, maxNextTime, pageRequest);
    }

    public boolean existsByExecutorId(long executorId) {
        return jobInfoRepository.existsByExecutorId(executorId);
    }

    public void batchUpdateTriggerInfo(List<JobView> viewList) {
        jobInfoRepository.batchUpdateTriggerInfo(viewList);
    }


    private long getNextTriggerTime(String cron) {
        long benchmark = System.currentTimeMillis() + PRE_READ_MS;
        Date nextValidTime;
        try {
            nextValidTime = new CronExpression(cron)
                    .getNextValidTimeAfter(new Date(benchmark));
        } catch (ParseException e) {
            String errMsg = e.getClass().getName() + ": " + e.getMessage();
            log.info("解析cron: {} 异常: {}", cron, errMsg);
            throw new VisibleException("解析cron表达式出现异常");
        }
        if (nextValidTime == null) {
            log.info("尝试通过cron: {} 获取下次执行时间失败", cron);
            throw new VisibleException("获取下次执行时间失败");
        }
        return nextValidTime.getTime();
    }
}
