package com.zzsong.job.scheduler.core.dispatcher.cluster;

import com.zzsong.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/10
 */
@Getter
@Setter
public class ConnectMessage {
  @Nonnull
  private String instanceId;

  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }


  public static ConnectMessage parseMessage(@Nonnull String message) {
    return JsonUtils.parseJson(message, ConnectMessage.class);
  }
}
