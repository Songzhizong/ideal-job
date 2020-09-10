package com.zzsong.job.common.loadbalancer;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/9
 */
public interface LbFactoryEventListener<Server extends LbServer> {

  void onChange(@Nonnull LbFactory<Server> lbFactory, @Nonnull LbFactoryEvent event);
}
