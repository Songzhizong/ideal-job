package cn.sh.ideal.job.scheduler.core.generator;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public interface IDGenerator {

  long generate();

  long generate(String biz);
}
