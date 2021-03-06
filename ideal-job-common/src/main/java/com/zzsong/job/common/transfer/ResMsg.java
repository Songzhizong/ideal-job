package com.zzsong.job.common.transfer;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2019-05-14
 */
public interface ResMsg {
  /**
   * 返回码
   */
  int code();

  /**
   * 响应描述
   */
  @Nonnull
  String message();
}
