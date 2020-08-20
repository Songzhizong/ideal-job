package cn.sh.ideal.nj.cmpt.job.common.res;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public enum CommonResMsg implements ResMsg {
  SUCCESS(200, "Success"),
  FOUND(302, "Found"),
  BAD_REQUEST(400, "Bad request"),
  UNAUTHORIZED(401, "Unauthorized"),
  NOT_FOUND(404, "Not found"),
  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
  ;

  private final int code;
  @Nonnull
  private final String message;

  CommonResMsg(int code,
               @Nonnull String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public int code() {
    return code;
  }

  @Nonnull
  @Override
  public String message() {
    return message;
  }
}
