package cn.sh.ideal.job.common.constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public enum BlockStrategyEnum {
  /**
   * 串行执行
   */
  SERIAL(1, "串行执行"),
  /**
   * 丢弃后续调度
   */
  DISCARD_LATER(2, "丢弃后续调度"),
  /**
   * 覆盖掉之前的调度
   */
  COVER_EARLY(3, "覆盖掉之前的调度"),
  ;

  @Nullable
  public static BlockStrategyEnum valueOfCode(int code) {
    switch (code) {
      case 1:
        return SERIAL;
      case 2:
        return DISCARD_LATER;
      case 3:
        return COVER_EARLY;
      default:
        return null;
    }
  }

  private final int code;
  @Nonnull
  private final String desc;

  BlockStrategyEnum(int code, @Nonnull String desc) {
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
