package cn.sh.ideal.job.scheduler.core.generator;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author 宋志宗
 * @date 2020/7/14
 */
@Component
public class JpaIdentityGenerator extends IdentityGenerator implements Configurable {
  private String biz;
  private static IDGenerator idGenerator;

  @Autowired
  public void setIDGenerator(IDGenerator idGenerator) {
    JpaIdentityGenerator.idGenerator = idGenerator;
  }

  public static void setIdGenerator(IDGenerator idGenerator) {
    JpaIdentityGenerator.idGenerator = idGenerator;
  }

  @Override
  public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
    this.biz = params.getProperty("biz");
  }

  @Override
  public Serializable generate(SharedSessionContractImplementor s, Object obj) {
    return idGenerator.generate(biz);
  }
}
