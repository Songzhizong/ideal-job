package com.zzsong.job.scheduler.core.conf;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.ResMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 统一异常处理类
 *
 * @author 宋志宗 on 2020/9/6
 */
public final class ExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

  /**
   * 处理接口请求异常, 统一返回包装信息
   *
   * @param throwable 异常信息
   * @param <T>       响应body泛型
   * @return Res
   */
  @Nonnull
  public static <T> Mono<? extends Res<T>> resultException(Throwable throwable) {
    if (throwable instanceof VisibleException) {
      //VisibleException
      VisibleException visibleException = (VisibleException) throwable;
      log.info("exception: ", visibleException);
      ResMsg resMsg = visibleException.getResMsg();
      if (resMsg == null) {
        return Mono.just(Res.err(throwable.getMessage()));
      } else {
        return Mono.just(Res.err(resMsg, throwable.getMessage()));
      }
    } else {
      // Other exception
      String errMsg = throwable.getMessage();
      if (errMsg == null) {
        errMsg = throwable.getClass().getName();
      }
      log.warn("exception: ", throwable);
      return Mono.just(Res.err(errMsg));
    }
  }
}
