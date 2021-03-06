package com.zzsong.job.scheduler.core.conf;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2020/9/9
 */
@Getter
@Setter
public class ClusterProperties {
  /**
   * 节点列表
   */
  private String nodes;
}
