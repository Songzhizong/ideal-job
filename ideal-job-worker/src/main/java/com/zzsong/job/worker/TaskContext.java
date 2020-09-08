package com.zzsong.job.worker;

import com.zzsong.job.common.message.payload.TaskParam;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗
 * @date 2020/9/8
 */
@Getter
@Setter
public class TaskContext {
  private TaskParam taskParam;

}
