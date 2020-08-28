package cn.sh.ideal.job.common.constants;

import javax.annotation.Nonnull;

/**
 * 执行模式
 *
 * @author 宋志宗
 * @date 2020/8/28
 */
public enum ExecuteTypeEnum {
  /**
   * JobHandler
   */
  JOB_HANDLER(0, "Job handler"),
  /**
   * Http script
   */
  HTTP_SCRIPT(1, "Http script"),
  /**
   * SpringCloud http script
   */
  LB_HTTP_SCRIPT(2, "SpringCloud http script"),
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
