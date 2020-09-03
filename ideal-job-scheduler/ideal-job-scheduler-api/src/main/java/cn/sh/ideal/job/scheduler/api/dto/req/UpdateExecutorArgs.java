package cn.sh.ideal.job.scheduler.api.dto.req;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class UpdateExecutorArgs {
    /**
     * 执行器Id
     */
    @Nonnull
    @NotNull(message = "执行器Id不能为空")
    private Long executorId;
    /**
     * 执行器AppName
     */
    @Nonnull
    @NotBlank(message = "appName不能为空")
    private String appName;
    /**
     * 执行器名称
     */
    @Nonnull
    @NotBlank(message = "执行器名称不能为空")
    private String title;
}
