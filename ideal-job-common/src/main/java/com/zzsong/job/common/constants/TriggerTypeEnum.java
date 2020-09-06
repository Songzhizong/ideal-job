package com.zzsong.job.common.constants;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public enum TriggerTypeEnum {
  MANUAL(1, "手动"),
  CRON(2, "CRON"),
  ;
  private final int code;
  @Nonnull
  private final String desc;

  TriggerTypeEnum(int code, @Nonnull String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  @Nonnull
  public String getDesc() {
    return desc;
  }
}
