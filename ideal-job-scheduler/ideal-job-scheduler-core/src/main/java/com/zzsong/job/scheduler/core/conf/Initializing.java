package com.zzsong.job.scheduler.core.conf;

import com.zzsong.job.scheduler.core.dispatch.TimingSchedule;
import com.zzsong.job.scheduler.core.generator.IDGenerator;
import com.zzsong.job.scheduler.core.generator.JpaIdentityGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
@Component
public class Initializing implements InitializingBean {
  private final IDGenerator idGenerator;
  private final TimingSchedule timingSchedule;

  public Initializing(IDGenerator idGenerator,
                      TimingSchedule timingSchedule) {
    this.idGenerator = idGenerator;
    this.timingSchedule = timingSchedule;
  }

  @Override
  public void afterPropertiesSet() {
    JpaIdentityGenerator.setIDGenerator(idGenerator);
    timingSchedule.start();
  }


//  @Override
//  public void contextDestroyed(ServletContextEvent sce) {
//    // web容器销毁前先销毁定时调度器, 防止websocket连接先一步断开导致时间轮的消息没能发送出去
//    timingSchedule.stop();
//  }
}
