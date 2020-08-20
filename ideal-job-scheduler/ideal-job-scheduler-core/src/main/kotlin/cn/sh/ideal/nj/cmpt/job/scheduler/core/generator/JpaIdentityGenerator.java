package cn.sh.ideal.nj.cmpt.job.scheduler.core.generator;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author 宋志宗
 * @date 2020/7/14
 */
public class JpaIdentityGenerator extends IdentityGenerator implements Configurable {
  private String biz;
  private static IDGenerator idGenerator;

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
