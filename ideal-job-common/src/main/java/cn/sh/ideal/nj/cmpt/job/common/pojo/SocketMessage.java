package cn.sh.ideal.nj.cmpt.job.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocketMessage {
  /**
   * 消息类型
   */
  private int messageType;

  @Nonnull
  private String payload = "";

  /**
   * 消息类型枚举
   */
  public enum Type {
    /**
     * 客户端注册
     */
    REGISTER(0, "客户端注册"),
    /**
     * 心跳
     */
    HEARTBEAT(1, "心跳"),
    /**
     * 空闲度
     */
    IDLE_BEAT(2, "空闲度"),
    /**
     * 执行任务
     */
    EXECUTE(3, "执行任务"),
    /**
     * 任务执行结果回调
     */
    EXECUTE_CALLBACK(4, "任务执行结果回调"),
    ;

    @Nullable
    public static Type valueOfCode(int code) {
      switch (code) {
        case 0:
          return REGISTER;
        case 1:
          return HEARTBEAT;
        case 2:
          return IDLE_BEAT;
        case 3:
          return EXECUTE;
        default:
          return null;
      }

    }

    private final int code;
    @Nonnull
    private final String desc;

    Type(int code, @Nonnull String desc) {
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
}
