package cn.sh.ideal.job.common;

import org.apache.commons.lang3.NotImplementedException;

/**
 * @author 宋志宗
 * @date 2020/8/21
 */
public interface Destroyable {

  /**
   * 销毁对象
   */
  void destroy();

  /**
   * 从销毁状态进行恢复
   */
  default void recover() {
    throw new NotImplementedException("Destroyable.recover() not implemented");
  }

  /**
   * @return 是否已销毁
   */
  default boolean isDestroyed() {
    throw new NotImplementedException("Destroyable.isDestroyed() not implemented");
  }
}
