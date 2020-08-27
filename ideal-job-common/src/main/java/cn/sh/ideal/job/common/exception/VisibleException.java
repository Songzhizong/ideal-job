package cn.sh.ideal.job.common.exception;

import cn.sh.ideal.job.common.transfer.ResMsg;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public class VisibleException extends RuntimeException {

  private ResMsg resMsg = null;

  public VisibleException(@Nonnull String message) {
    super(message);
  }

  public VisibleException(@Nonnull ResMsg resMsg) {
    this(resMsg.message());
    this.resMsg = resMsg;
  }

  public VisibleException(@Nonnull ResMsg resMsg, String message) {
    this(message);
    this.resMsg = resMsg;
  }
}
