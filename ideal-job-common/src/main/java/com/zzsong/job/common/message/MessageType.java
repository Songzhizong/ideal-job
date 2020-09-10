package com.zzsong.job.common.message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/8/22
 */
public enum MessageType {
  /**
   * 客户端注册
   */
  REGISTER("register", "客户端注册"),
  /**
   * 客户端注册回调
   */
  REGISTER_CALLBACK("registerCallback", "客户端注册回调"),
//  /**
//   * 心跳
//   */
//  HEARTBEAT("heartbeat", "心跳"),
  /**
   * 空闲度
   */
  IDLE_BEAT("idleBeat", "空闲度"),
  /**
   * 空闲度结果回调
   */
  IDLE_BEAT_CALLBACK("idleBeatCallback", "空闲度结果回调"),
  /**
   * 执行任务
   */
  EXECUTE_JOB("executeJob", "执行任务"),
  /**
   * 任务执行结果回调
   */
  EXECUTE_JOB_CALLBACK("executeJobCallback", "任务执行结果回调"),
  ;

  private static final Map<String, MessageType> codeMapper = new HashMap<>();

  static {
    for (MessageType value : values()) {
      codeMapper.put(value.code, value);
    }
  }

  @Nullable
  public static MessageType valueOfCode(@Nonnull String code) {
    return codeMapper.get(code);
  }


  @Nonnull
  private final String code;
  @Nonnull
  private final String desc;

  MessageType(@Nonnull String code, @Nonnull String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Nonnull
  public String getCode() {
    return code;
  }

  @Nonnull
  public String getDesc() {
    return desc;
  }
}
