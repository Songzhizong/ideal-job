package com.zzsong.job.common.constants;

import javax.annotation.Nonnull;

/**
 * 执行模式
 *
 * @author 宋志宗 on 2020/8/28
 */
public enum ExecuteTypeEnum {
  /**
   * JobHandler
   */
  BEAN(0, "Bean"),
  /**
   * Http script
   */
  HTTP(1, "Http Script"),
  /**
   * SpringCloud http script
   */
  LB_HTTP(2, "Spring Cloud Http Script"),
  ;


  private final int code;
  @Nonnull
  private final String desc;

  ExecuteTypeEnum(int code, @Nonnull String desc) {
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
