package com.zzsong.job.common.transfer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * @author 宋志宗 on 2020/5/21
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Range<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  @Nullable
  private T start;
  @Nullable
  private T end;
}
