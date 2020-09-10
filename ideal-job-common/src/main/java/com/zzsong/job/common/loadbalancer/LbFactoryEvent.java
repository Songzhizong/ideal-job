package com.zzsong.job.common.loadbalancer;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2020/9/9
 */
@Getter
@Setter
public class LbFactoryEvent {
  /**
   * 服务名称
   */
  private String serverName;
  /**
   * 服务总数
   */
  private int serverCount;
  /**
   * 可达服务数
   */
  private int reachableServerCount;
}
