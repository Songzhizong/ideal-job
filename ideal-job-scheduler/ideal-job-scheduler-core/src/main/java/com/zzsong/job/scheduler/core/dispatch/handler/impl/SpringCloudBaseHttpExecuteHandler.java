package com.zzsong.job.scheduler.core.dispatch.handler.impl;

import com.zzsong.job.common.constants.ExecuteTypeEnum;
import com.zzsong.job.scheduler.core.admin.service.JobWorkerService;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.dispatch.handler.ExecuteHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/9/3
 */
@Component("springCloudHttpExecuteHandler")
public final class SpringCloudBaseHttpExecuteHandler extends BaseHttpExecuteHandler {
  private static final Logger log = LoggerFactory
      .getLogger(SpringCloudBaseHttpExecuteHandler.class);
  @Nullable
  private final SpringClientFactory springClientFactory;
  @Nonnull
  private final JobWorkerService jobWorkerService;

  protected SpringCloudBaseHttpExecuteHandler(
      @Nonnull JobInstanceService instanceService,
      @Nullable SpringClientFactory springClientFactory,
      @Nonnull JobWorkerService jobWorkerService) {
    super(instanceService);
    this.springClientFactory = springClientFactory;
    this.jobWorkerService = jobWorkerService;
    ExecuteHandlerFactory.register(ExecuteTypeEnum.LB_HTTP_SCRIPT, this);
  }

}
