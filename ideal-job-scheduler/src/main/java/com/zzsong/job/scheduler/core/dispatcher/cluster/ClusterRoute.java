package com.zzsong.job.scheduler.core.dispatcher.cluster;

/**
 * @author 宋志宗 on 2020/9/10
 */
public interface ClusterRoute {
  String CONNECT = "cluster-connect";
  String DISPATCH = "cluster-dispatch";
  String SUPPORT_APPS = "cluster-support-apps";
  String REFRESH_SUPPORT_NOTICE = "cluster-support-refresh";
}
