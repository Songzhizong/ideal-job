package cn.sh.ideal.job.executor.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public final class JobThreadFactory {
  /**
   * jobId -> JobThread
   */
  private static final ConcurrentMap<String, JobThread> JOB_THREAD_MAP = new ConcurrentHashMap<>();


  public static void register(@Nonnull String jobId, @Nonnull JobThread jobThread) {
    jobThread.start();
    JOB_THREAD_MAP.put(jobId, jobThread);
  }

  public static void remove(@Nonnull JobThread jobThread) {
    jobThread.destroy();
    String jobId = jobThread.getJobId();
    JOB_THREAD_MAP.remove(jobId);
  }

  @Nullable
  public static JobThread get(@Nonnull String jobId) {
    return JOB_THREAD_MAP.get(jobId);
  }

  /**
   * 尝试通过jobId获取JobThread, 如果没有则调用function并自动调用其start方法
   *
   * @param jobId    任务ID
   * @param function JobThread为空时调用此方法自动注册一个
   * @return JobThread
   * @author 宋志宗
   * @date 2020/8/23 12:12
   */
  @Nonnull
  public static JobThread computeIfAbsent(@Nonnull String jobId, Function<String, JobThread> function) {
    return JOB_THREAD_MAP.computeIfAbsent(jobId, k -> {
      JobThread thread = function.apply(jobId);
      if (thread == null) {
        throw new NullPointerException("Function compute result is null");
      }
      thread.start();
      return thread;
    });
  }
}
