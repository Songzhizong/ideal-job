package com.zzsong.job.scheduler.core.admin.storage;

import reactor.core.publisher.Mono;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
public interface JobInfoStorage {
    /**
     * 执行器是否存在任务
     */
    Mono<Boolean> existsByWorkerId(long workerId);
}
