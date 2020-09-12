package com.zzsong.job.scheduler.core.admin.controller;

import com.zzsong.job.common.transfer.Paging;
import com.zzsong.job.common.transfer.Res;
import com.zzsong.job.scheduler.api.client.JobInstanceClient;
import com.zzsong.job.scheduler.api.dto.req.QueryInstanceArgs;
import com.zzsong.job.scheduler.core.admin.vo.JobInstanceVo;
import com.zzsong.job.scheduler.core.admin.service.JobInstanceService;
import com.zzsong.job.scheduler.core.converter.JobInstanceConverter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务实例
 *
 * @author 宋志宗 on 2020/8/20
 */
@RestController
@RequestMapping("/job/instance")
public class JobInstanceController implements JobInstanceClient {
  @Nonnull
  private final JobInstanceService instanceService;

  public JobInstanceController(@Nonnull JobInstanceService instanceService) {
    this.instanceService = instanceService;
  }

  /**
   * 前端查询任务实例列表
   *
   * @param args   查询参数
   * @param paging 分页参数
   * @return 任务实例VO列表
   * @author 宋志宗 on 2020/9/9
   */
  @Nonnull
  @PostMapping("/queryVo")
  public Mono<Res<List<JobInstanceVo>>> query(@RequestBody @Nonnull QueryInstanceArgs args,
                                              @Nullable Paging paging) {
    if (paging == null) {
      paging = Paging.of(1, 20);
    }
    return instanceService.query(args, paging)
        .map(res ->
            res.convertData(list ->
                list.stream()
                    .map(JobInstanceConverter::toJobInstanceVo)
                    .collect(Collectors.toList())
            )
        );
  }
}
