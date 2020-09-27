package com.zzsong.job.scheduler.core.admin.service;

import com.zzsong.job.common.cache.ReactiveCache;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.utils.DateTimes;
import com.zzsong.job.common.utils.JsonUtils;
import com.zzsong.job.scheduler.api.dto.req.CreateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.QueryExecutorArgs;
import com.zzsong.job.scheduler.api.dto.req.UpdateExecutorArgs;
import com.zzsong.job.scheduler.api.dto.rsp.JobExecutorRsp;
import com.zzsong.job.scheduler.core.dispatcher.ClusterNode;
import com.zzsong.job.scheduler.core.dispatcher.cluster.ClusterRegistry;
import com.zzsong.job.scheduler.core.pojo.JobExecutor;
import com.zzsong.job.scheduler.core.admin.storage.JobInfoStorage;
import com.zzsong.job.scheduler.core.admin.storage.JobExecutorStorage;
import com.zzsong.job.scheduler.core.converter.JobExecutorConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/2
 */
@SuppressWarnings("DuplicatedCode")
@Service
public class JobExecutorService {
  private static final Logger log = LoggerFactory.getLogger(JobExecutorService.class);
  private static final String CACHE_NAME = "ideal:job:cache:executor:";
  private static final String UN_CACHE_VALUE = "UN_CACHE";
  private static final Duration CACHE_EXPIRE = Duration.ofDays(1);
  private static final Duration UN_CACHE_EXPIRE = Duration.ofMinutes(60);

  @Autowired
  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  private ClusterRegistry clusterRegistry;

  private final ReactiveCache reactiveCache;
  private final JobInfoStorage jobInfoStorage;
  private final JobExecutorStorage jobExecutorStorage;

  public JobExecutorService(ReactiveCache reactiveCache,
                            JobInfoStorage jobInfoStorage,
                            JobExecutorStorage jobExecutorStorage) {
    this.reactiveCache = reactiveCache;
    this.jobInfoStorage = jobInfoStorage;
    this.jobExecutorStorage = jobExecutorStorage;
  }

  public Mono<JobExecutorRsp> create(@Nonnull CreateExecutorArgs args) {
    String appName = args.getAppName();
    String title = args.getTitle();
    return jobExecutorStorage.findByAppName(appName)
        .flatMap(op -> {
          if (op.isPresent()) {
            log.info("appName: {} 已存在", appName);
            return Mono.error(new VisibleException("appName已存在"));
          } else {
            LocalDateTime now = DateTimes.now();
            JobExecutor executor = new JobExecutor();
            executor.setAppName(appName);
            executor.setTitle(title);
            executor.setCreatedTime(now);
            executor.setUpdateTime(now);
            return jobExecutorStorage.save(executor)
                .flatMap(savedExecutor -> {
                  long executorId = savedExecutor.getExecutorId();
                  String key = CACHE_NAME + executorId;
                  String value = JsonUtils.toJsonString(savedExecutor);
                  log.debug("新增执行器: {}", value);
                  return reactiveCache.set(key, value, CACHE_EXPIRE)
                      .map(b -> JobExecutorConverter.toJobExecutorRsp(savedExecutor));
                });
          }
        });
  }

  @Nonnull
  public Mono<JobExecutorRsp> update(@Nonnull UpdateExecutorArgs updateArgs) {
    Long executorId = updateArgs.getExecutorId();
    String appName = updateArgs.getAppName();
    String title = updateArgs.getTitle();
    Mono<Optional<JobExecutor>> byAppName = jobExecutorStorage.findByAppName(appName);
    Mono<Optional<JobExecutor>> byId = jobExecutorStorage.findById(executorId);
    return Mono.zip(byAppName, byId)
        .flatMap(t -> {
          Optional<Long> byAppNameId = t.getT1().map(JobExecutor::getExecutorId);
          Optional<JobExecutor> jobExecutorOptional = t.getT2();
          Long a;
          if (byAppNameId.isPresent() && !(a = byAppNameId.get()).equals(executorId)) {
            log.info("appName: {} 已被: {} 使用", appName, a);
            return Mono.error(new VisibleException("appName已被使用"));
          }
          if (!jobExecutorOptional.isPresent()) {
            log.info("执行器: {} 不存在", executorId);
            return Mono.error(new VisibleException("执行器不存在"));
          }
          LocalDateTime now = LocalDateTime.now();
          JobExecutor executor = jobExecutorOptional.get();
          executor.setAppName(appName);
          executor.setTitle(title);
          executor.setUpdateTime(now);
          return jobExecutorStorage.save(executor)
              .flatMap(savedExecutor -> {
                String key = CACHE_NAME + executorId;
                String value = JsonUtils.toJsonString(savedExecutor);
                return reactiveCache.set(key, value, CACHE_EXPIRE)
                    .map(b -> JobExecutorConverter.toJobExecutorRsp(executor));
              });
        });
  }

  public Mono<Integer> delete(long executorId) {
    return jobInfoStorage.existsByExecutorId(executorId)
        .flatMap(exist -> {
          if (exist) {
            // 执行器存在定时任务则无法删除
            return Mono.error(new VisibleException("该执行器存在定时任务"));
          } else {
            return jobExecutorStorage.findById(executorId)
                .flatMap(op -> {
                  // 执行器不存在, 直接返回结果
                  if (!op.isPresent()) {
                    log.info("执行器: {} 不存在", executorId);
                    return Mono.just(0);
                  }
                  // 执行器存在, 删除执行器, 删除缓存
                  JobExecutor jobExecutor = op.get();
                  if (log.isDebugEnabled()) {
                    log.debug("删除执行器: {}", JsonUtils.toJsonString(jobExecutor));
                  }
                  return jobExecutorStorage.delete(jobExecutor.getExecutorId())
                      .flatMap(count -> {
                        String key = CACHE_NAME + executorId;
                        return reactiveCache.delete(key)
                            .map(b -> count);
                      });
                });
          }
        });
  }

  @Nonnull
  public Mono<Res<List<JobExecutorRsp>>> query(@Nonnull QueryExecutorArgs args,
                                               @Nonnull Paging paging) {
    return jobExecutorStorage.query(args, paging)
        .map(listRes -> {
          if (listRes.getData() == null || listRes.getData().size() == 0) {
            return listRes.convertData(list -> Collections.emptyList());
          }
          // 集群注册表信息
          Map<ClusterNode, Map<String, List<String>>> clusterRegistryDetails
              = clusterRegistry.getClusterRegistryDetails();
          // 集群实例列表
          List<String> nodeInstances = clusterRegistryDetails.keySet()
              .stream().map(ClusterNode::getInstanceId)
              .collect(Collectors.toList());
          // 执行器在各个机器节点的注册情况 executor appName -> cluster node instance -> online executor node
          Map<String, Map<String, List<String>>> map = new HashMap<>();
          clusterRegistryDetails.forEach((node, stringListMap) ->
              stringListMap.forEach((appName, instanceList) -> {
                Map<String, List<String>> listMap = map
                    .computeIfAbsent(appName, k -> new HashMap<>());
                listMap.put(node.getInstanceId(), instanceList);
              }));
          return listRes.convertData(list ->
              list.stream()
                  .map(executor -> {
                    final JobExecutorRsp executorRsp = JobExecutorConverter.toJobExecutorRsp(executor);
                    final String appName = executorRsp.getAppName();
                    // cluster nodeInstance -> online executor node
                    Map<String, List<String>> nodeRegistry = new HashMap<>();
                    for (String nodeInstance : nodeInstances) {
                      final Map<String, List<String>> stringListMap = map.get(appName);
                      if (stringListMap == null) {
                        nodeRegistry.put(nodeInstance, Collections.emptyList());
                      } else {
                        executorRsp.setOnline(true);
                        final List<String> strings = stringListMap.get(nodeInstance);
                        if (strings == null) {
                          nodeRegistry.put(nodeInstance, Collections.emptyList());
                        } else {
                          nodeRegistry.put(nodeInstance, strings);
                        }
                      }
                    }
                    executorRsp.setNodeRegistry(nodeRegistry);
                    return executorRsp;
                  })
                  .collect(Collectors.toList()));
        });
  }

  @Nonnull
  public Mono<Optional<JobExecutor>> loadById(long executorId) {
    String key = CACHE_NAME + executorId;
    return reactiveCache.get(key).defaultIfEmpty("")
        .flatMap(cache -> {
          // 如果缓存中存在, 直接返回缓存的内容
          if (StringUtils.isNotBlank(cache)) {
            JobExecutor executor = JsonUtils.parseJson(cache, JobExecutor.class);
            return Mono.just(Optional.of(executor));
          } else if (UN_CACHE_VALUE.equals(cache)) {
            return Mono.just(Optional.empty());
          } else {
            // 缓存不存在, 尝试从库中获取
            return jobExecutorStorage.findById(executorId)
                .flatMap(load -> {
                  if (!load.isPresent()) {
                    // 如果库中也不存在, 缓存结果并返回empty
                    return reactiveCache
                        .set(key, UN_CACHE_VALUE, UN_CACHE_EXPIRE)
                        .map(b -> Optional.empty());
                  } else {
                    // 库中存在, 刷入缓存, 返回执行器信息
                    JobExecutor executor = load.get();
                    String value = JsonUtils.toJsonString(executor);
                    return reactiveCache.set(key, value, CACHE_EXPIRE)
                        .map(b -> Optional.of(executor));
                  }
                });
          }
        });
  }
}
