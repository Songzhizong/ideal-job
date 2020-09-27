package com.zzsong.job.scheduler.core.event;

import lombok.Getter;
import lombok.Setter;

/**
 * 接收到Worker上报执行日志事件
 * <p>当worker向调度器发送执行日志时会触发此事件</p>
 *
 * @author 宋志宗 on 2020/9/9
 */
@Getter
@Setter
public class ReceiveTaskLogEvent {

}
