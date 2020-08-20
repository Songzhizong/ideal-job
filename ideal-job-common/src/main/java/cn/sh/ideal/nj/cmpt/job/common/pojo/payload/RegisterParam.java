package cn.sh.ideal.nj.cmpt.job.common.pojo.payload;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class RegisterParam implements Serializable {
  private static final long serialVersionUID = -1L;
  /**
   * 鉴权token
   */
  private String accessToken;
  
  /**
   * 权重
   */
  private int weight = 1;
}
