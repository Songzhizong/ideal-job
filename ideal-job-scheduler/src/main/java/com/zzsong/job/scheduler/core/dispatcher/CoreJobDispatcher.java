package com.zzsong.job.scheduler.core.dispatcher;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.common.constants.TriggerTypeEnum;
import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.loadbalancer.*;
import com.zzsong.job.common.loadbalancer.strategy.WeightRandomLoadBalancer;
import com.zzsong.job.common.transfer.CommonResMsg;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.core.admin.service.JobWorkerService;
import com.zzsong.job.scheduler.core.dispatcher.cluster.ClusterRegistry;
import com.zzsong.job.scheduler.core.pojo.JobView;
import com.zzsong.job.scheduler.core.pojo.JobWorker;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 核心调度器
 * <p>所有的调度触发都会通过此调度器完成调度</p>
 *
 * @author 宋志宗 on 2020/9/9
 */
@Component("jobDispatcher")
public class CoreJobDispatcher implements JobDispatcher {
  private static final Logger log = LoggerFactory.getLogger(CoreJobDispatcher.class);
  private static final LoadBalancer<ClusterNode> LOAD_BALANCER = new WeightRandomLoadBalancer<>();

  @Setter
  private boolean clusterEnabled = false;
  @Autowired
  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  private ClusterRegistry registry;
  private final JobWorkerService workerService;
  private final LocalClusterNode localClusterDispatcher;

  public CoreJobDispatcher(JobWorkerService workerService,
                           LocalClusterNode localClusterDispatcher) {
    this.workerService = workerService;
    this.localClusterDispatcher = localClusterDispatcher;

  }

  @Override
  public Mono<Res<Void>> dispatch(@Nonnull JobView jobView,
                                  @Nonnull TriggerTypeEnum triggerType,
                                  @Nullable String customExecuteParam) {
    // 如果没有开启集群, 那么就直接在本地完成调度
    if (!clusterEnabled) {
      return localClusterDispatcher.dispatch(jobView, triggerType, customExecuteParam);
    }
    ExecuteTypeEnum executeType = jobView.getExecuteType();

    // JOB_HANDLER模式下 worker不一定会连接到集群中的每一个节点, 因此需要获取到其连接到的节点列表, 然后执行调度
    if (executeType == ExecuteTypeEnum.BEAN) {
      long workerId = jobView.getWorkerId();
      return workerService.loadById(workerId)
          .flatMap(workerOptional -> {
            if (!workerOptional.isPresent()) {
              log.info("任务: {} 调度失败, 执行器: {} 不存在", jobView.getJobId(), workerId);
              return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND,
                  "执行器: " + workerId + "不存在"));
            }
            JobWorker jobWorker = workerOptional.get();
            String appName = jobWorker.getAppName();
            if (registry.isCurrentNodeSupport(appName)) {
              return localClusterDispatcher.dispatch(jobView, triggerType, customExecuteParam);
            } else {
              // 如果本地注册表中没有该应用, 选取一个包含此应用的节点执行
              final List<ClusterNode> availableNodes = registry.getSupportNodes(appName);
              final ClusterNode dispatcher = LOAD_BALANCER.chooseServer(null, availableNodes);
              if (dispatcher == null) {
                // 可能该worker没有注册到集群
                log.warn("本地注册表不包含应用: {}, 选取远程节点返回空", appName);
                return Mono.just(Res.err("选取ClusterDispatcher为空"));
              }
              String instanceId = dispatcher.getInstanceId();
              log.info("本地注册表不包含应用: {}, 尝试通过远程节点: {} 完成调度", appName, instanceId);
              return dispatcher.dispatch(jobView, triggerType, customExecuteParam);
            }
          });
    }

    // http script 模式直接在本地调度
    return localClusterDispatcher.dispatch(jobView, triggerType, customExecuteParam);
  }
}
