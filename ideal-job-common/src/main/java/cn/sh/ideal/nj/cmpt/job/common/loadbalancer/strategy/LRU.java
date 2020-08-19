package cn.sh.ideal.nj.cmpt.job.common.loadbalancer.strategy;

import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServer;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LbServerFactory;
import cn.sh.ideal.nj.cmpt.job.common.loadbalancer.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 最近最久未使用
 *
 * @author 宋志宗
 * @date 2020/8/19
 */
public class LRU implements LoadBalancer {

  @Override
  @Nullable
  public LbServer chooseServer(@Nullable Object key, @Nonnull LbServerFactory factory) {
    return null;
  }
}
