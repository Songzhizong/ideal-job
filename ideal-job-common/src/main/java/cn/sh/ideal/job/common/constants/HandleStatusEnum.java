package cn.sh.ideal.job.common.constants;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public enum HandleStatusEnum {
  WAITING(0, "等待执行"),
  RUNNING(1, "执行中"),
  COMPLETE(2, "执行完成"),
  ABNORMAL(3, "执行异常"),
  ;

  @SuppressWarnings("DuplicateBranchesInSwitch")
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
      default:
        return ABNORMAL;
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
