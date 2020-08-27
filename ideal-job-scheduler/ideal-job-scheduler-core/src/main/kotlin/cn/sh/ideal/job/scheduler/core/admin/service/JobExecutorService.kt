package cn.sh.ideal.job.scheduler.core.admin.service

import cn.sh.ideal.job.common.exception.VisibleException
import cn.sh.ideal.job.common.transfer.Paging
import cn.sh.ideal.job.common.transfer.Res
import cn.sh.ideal.job.common.transfer.SpringPages
import cn.sh.ideal.job.scheduler.api.dto.req.CreateExecutorArgs
import cn.sh.ideal.job.scheduler.api.dto.req.QueryExecutorArgs
import cn.sh.ideal.job.scheduler.api.dto.req.UpdateExecutorArgs
import cn.sh.ideal.job.scheduler.api.dto.rsp.ExecutorInfoRsp
import cn.sh.ideal.job.scheduler.core.admin.entity.JobExecutor
import cn.sh.ideal.job.scheduler.core.admin.repository.JobExecutorRepository
import cn.sh.ideal.job.scheduler.core.converter.ExecutorConverter
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.ArrayList
import javax.persistence.criteria.Predicate

/**
 * @author 宋志宗
 * @date 2020/8/24
 */
@Service
class JobExecutorService(private val jobExecutorRepository: JobExecutorRepository) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(JobExecutorService::class.java)
    private const val cacheName = "ideal:job:cache:executor"
  }

  @Autowired
  private lateinit var jobService: JobService

  @Suppress("SpringElInspection")
  @CachePut(value = [cacheName], key = "#result.executorId")
  fun create(args: CreateExecutorArgs): JobExecutor {
    val appName = args.appName
    val title = args.title
    val byAppName = jobExecutorRepository.findTopByAppName(appName)
    if (byAppName != null) {
      throw VisibleException("appName已存在")
    }
    val executor = JobExecutor()
    executor.appName = appName
    executor.title = title
    jobExecutorRepository.save(executor)
    return executor
  }

  @CachePut(value = [cacheName], key = "#updateArgs.executorId")
  fun update(updateArgs: UpdateExecutorArgs): JobExecutor {
    val executorId = updateArgs.executorId
    val appName = updateArgs.appName
    val title = updateArgs.title
    val byAppName = jobExecutorRepository.findTopByAppName(appName)
    val byAppNameExecutorId = byAppName?.executorId
    if (byAppName != null && byAppNameExecutorId != executorId) {
      log.info("appName: {} 已被: {} 使用", appName, byAppNameExecutorId)
      throw VisibleException("appName已被使用")
    }
    val executor = jobExecutorRepository.findByIdOrNull(executorId)
    if (executor == null) {
      log.info("执行器: {} 不存在")
      throw VisibleException("执行器不存在")
    }
    executor.appName = appName
    executor.title = title
    jobExecutorRepository.save(executor)
    return executor
  }

  @CacheEvict(value = [cacheName], key = "#executorId")
  fun delete(executorId: Long) {
    val existJob = jobService.existsByExecutorId(executorId)
    if (existJob) {
      throw VisibleException("该执行器存在定时任务")
    }
    val executor = jobExecutorRepository.findByIdOrNull(executorId)
    if (executor == null) {
      log.info("执行器: {} 不存在", executorId)
      return
    }
    jobExecutorRepository.delete(executor)
  }

  fun query(args: QueryExecutorArgs, paging: Paging): Res<List<ExecutorInfoRsp>> {
    val appName = args.appName
    val title = args.title
    val page = jobExecutorRepository.findAll(
        { root, cq, cb ->
          val predicates = ArrayList<Predicate>()
          if (StringUtils.isNotBlank(appName)) {
            predicates.add(cb.equal(root.get<Any>("appName"), appName))
          }
          if (StringUtils.isNotBlank(title)) {
            predicates.add(cb.equal(root.get<Any>("title"), title))
          }
          cq.where(*predicates.toTypedArray()).restriction
        }, SpringPages.paging2Pageable(paging)
    )
    return SpringPages.toPageRes(page) { ExecutorConverter.toExecutorInfoRsp(it) }
  }

  @Cacheable(value = [cacheName], key = "#executorId")
  fun loadById(executorId: Long): JobExecutor? {
    return jobExecutorRepository.findByIdOrNull(executorId)
  }
}