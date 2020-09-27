package com.zzsong.job.scheduler.core.dispatcher;

import com.zzsong.job.common.loadbalancer.LbServer;

/**
 * 集群调度器接口
 *
 * @author 宋志宗 on 2020/9/9
 */
public interface ClusterNode extends JobDispatcher, LbServer {

}
