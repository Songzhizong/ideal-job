package cn.sh.ideal.job.common.worker;

import cn.sh.ideal.job.common.message.payload.TaskCallback;
import cn.sh.ideal.job.common.message.payload.IdleBeatCallback;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface RemoteTaskWorker extends TaskWorker {
    /**
     * 任务执行完成回调
     *
     * @param callback 回调消息
     * @author 宋志宗
     * @date 2020/8/22 23:47
     */
    void taskCallback(@Nonnull TaskCallback callback);
}
