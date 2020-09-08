package com.zzsong.job.worker;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗
 * @date 2020/9/8
 */
public class TaskContextHolder {
  private static final ConcurrentMap<Thread, TaskContext> CONTEXT_MAP = new ConcurrentHashMap<>();

  static void putContext(@Nonnull TaskContext context) {
    CONTEXT_MAP.put(Thread.currentThread(), context);
  }

  static void removeContext() {
    CONTEXT_MAP.remove(Thread.currentThread());
  }

  @Nonnull
  public static TaskContext getCurrentContext() {
    final Thread currentThread = Thread.currentThread();
    final String threadName = currentThread.getName();
    final TaskContext taskContext = CONTEXT_MAP.get(currentThread);
    if (taskContext == null) {
      throw new RuntimeException("Thread: " + threadName + " context not found");
    }
    return taskContext;
  }
}
