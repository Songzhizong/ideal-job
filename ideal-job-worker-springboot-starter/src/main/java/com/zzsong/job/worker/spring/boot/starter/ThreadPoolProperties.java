package com.zzsong.job.worker.spring.boot.starter;

/**
 * @author 宋志宗
 * @date 2020/8/25
 */
public class ThreadPoolProperties {
    private int corePoolSize = -1;
    private int maximumPoolSize = -1;
    private int workQueueSize = 200;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getWorkQueueSize() {
        return workQueueSize;
    }

    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }
}
