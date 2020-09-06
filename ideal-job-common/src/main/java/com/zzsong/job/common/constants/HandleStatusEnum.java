package com.zzsong.job.common.constants;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public enum HandleStatusEnum {
  /**
   * 等待执行
   */
  WAITING(0, "等待执行"),
  /**
   * 执行中
   */
  RUNNING(1, "执行中"),
  /**
   * 执行完成
   */
  COMPLETE(2, "执行完成"),
  /**
   * 执行异常
   */
  ABNORMAL(3, "执行异常"),
  /**
   * 丢弃
   */
  DISCARD(4, "丢弃"),
  /**
   * 未知
   */
  UNKNOWN(-1, "未知"),
  ;

  public static HandleStatusEnum valueOfCode(int code) {
    switch (code) {
      case 0:
        return WAITING;
      case 1:
        return RUNNING;
      case 2:
        return COMPLETE;
      case 3:
        return ABNORMAL;
      case 4:
        return DISCARD;
      default:
        return UNKNOWN;
    }
  }

  private final int code;
  @Nonnull
  private final String desc;

  HandleStatusEnum(int code, @Nonnull String desc) {
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
