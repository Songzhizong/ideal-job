package cn.sh.ideal.job.common.worker;

import cn.sh.ideal.job.common.message.payload.TaskParam;
import cn.sh.ideal.job.common.loadbalancer.LbServer;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface TaskWorker extends LbServer {

    /**
     * 执行任务
     *
     * @param param 触发器参数
     * @author 宋志宗
     * @date 2020/8/20 2:12 下午
     */
    void execute(@Nonnull TaskParam param);
}
