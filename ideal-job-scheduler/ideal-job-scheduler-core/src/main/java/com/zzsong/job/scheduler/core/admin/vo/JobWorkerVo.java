package com.zzsong.job.scheduler.core.admin.vo;

import com.zzsong.job.scheduler.api.dto.rsp.JobWorkerRsp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by 宋志宗 on 2020/9/11
 */
@Getter
@Setter
public class JobWorkerVo extends JobWorkerRsp {
  /**
   * 是否在线
   */
  private boolean online = false;
  /**
   * 各个节点的注册情况
   */
  private Map<String, List<String>> nodeRegistry;
}
