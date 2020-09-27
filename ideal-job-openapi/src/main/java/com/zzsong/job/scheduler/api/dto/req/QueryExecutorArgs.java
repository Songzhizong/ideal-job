package com.zzsong.job.scheduler.api.dto.req;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/8/26
 */
@Getter
@Setter
public class QueryExecutorArgs {
  /**
   * 执行器AppName
   */
  @Nullable
  private String appName;
  /**
   * 执行器名称
   */
  @Nullable
  private String title;

  /**
   * @return return "" if empty
   */
  @Nonnull
  public String toQueryString() {
    final StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(appName)) {
      builder.append("appName=").append(appName);
    }
    if (StringUtils.isNotBlank(title)) {
      if (StringUtils.isNotBlank(builder)) {
        builder.append("&title=").append(title);
      } else {
        builder.append("title=").append(title);
      }
    }
    return builder.toString();
  }
}
