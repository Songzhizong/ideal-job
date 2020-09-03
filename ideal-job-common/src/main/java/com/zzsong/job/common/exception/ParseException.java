package com.zzsong.job.common.exception;

/**
 * @author 宋志宗
 * @date 2020/8/22
 */
public class ParseException extends Exception {
    public ParseException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
