package com.zzsong.job.scheduler.core.conf;

import com.zzsong.job.common.exception.VisibleException;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.common.transfer.ResMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import javax.annotation.Nonnull;

/**
 * Created by 宋志宗 on 2020/9/12
 */
@RestControllerAdvice
public class CustomExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);
  private static final MultiValueMap<String, String> RESPONSE_HEADERS = new LinkedMultiValueMap<>();

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<Object> bindExceptionHandler(@Nonnull WebExchangeBindException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .reduce("", (sum, item) -> {
          if ("".equals(sum)) {
            return item;
          }
          return sum + "," + item;
        });
    log.debug("@Valid fail : {}", message);
    Res<Object> body = Res.err(message);
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.OK);
  }

  @ExceptionHandler(VisibleException.class)
  public ResponseEntity<Object> globalExceptionHandler(@Nonnull VisibleException exception) {
    String message = exception.getMessage();
    log.debug("AlertException: {}", message);
    Res<Object> body = Res.err(message);
    ResMsg resMsg = exception.getResMsg();
    if (resMsg != null) {
      body.setCode(resMsg.code());
    }
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.OK);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> globalExceptionHandler(Exception exception) {
    log.warn("Exception: ", exception);
    String message;
    if (exception.getMessage() != null) {
      message = exception.getMessage();
    } else {
      message = exception.getClass().getSimpleName();
    }
    Res<Object> body = Res.err(message);
    return new ResponseEntity<>(body, RESPONSE_HEADERS, HttpStatus.OK);
  }
}
