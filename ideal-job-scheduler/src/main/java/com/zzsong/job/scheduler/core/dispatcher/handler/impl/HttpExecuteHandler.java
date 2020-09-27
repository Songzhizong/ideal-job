package com.zzsong.job.scheduler.core.dispatcher.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.dispatcher.handler.ExecuteHandlerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/3
 */
@Component("httpExecuteHandler")
public final class HttpExecuteHandler extends BaseHttpExecuteHandler {

  public HttpExecuteHandler(@Nonnull JobInstanceService instanceService) {
    super(instanceService);
    ExecuteHandlerFactory.register(ExecuteTypeEnum.HTTP, this);
  }
}
