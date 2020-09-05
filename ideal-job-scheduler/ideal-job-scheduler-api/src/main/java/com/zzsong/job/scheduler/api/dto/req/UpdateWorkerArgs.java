package com.zzsong.job.scheduler.api.dto.req;

import com.zzsong.job.common.exception.VisibleException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/26
 */
@Getter
@Setter
public class UpdateWorkerArgs {
    /**
     * 执行器Id
     */
    private long workerId = -1L;
    /**
     * 执行器AppName
     */
    @Nonnull
    private String appName = "";
    /**
     * 执行器名称
     */
    @Nonnull
    private String title = "";

    public UpdateWorkerArgs checkArgs() {
        if (workerId < 1) {
            throw new VisibleException("执行器Id不合法");
        }
        if (StringUtils.isBlank(this.appName)) {
            throw new VisibleException("appName不能为空");
        }
        if (StringUtils.isBlank(this.title)) {
            throw new VisibleException("执行器名称不能为空");
        }
        return this;
    }
}
