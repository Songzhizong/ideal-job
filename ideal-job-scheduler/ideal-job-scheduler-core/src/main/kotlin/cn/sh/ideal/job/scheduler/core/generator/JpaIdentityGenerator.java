package cn.sh.ideal.job.scheduler.core.generator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author 宋志宗
 * @date 2020/7/14
 */
@Component
public class JpaIdentityGenerator extends IdentityGenerator {
    private static IDGenerator idGenerator;

    @Autowired
    public void setIDGenerator(IDGenerator idGenerator) {
        JpaIdentityGenerator.idGenerator = idGenerator;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor s, Object obj) {
        return idGenerator.generate();
    }
}
