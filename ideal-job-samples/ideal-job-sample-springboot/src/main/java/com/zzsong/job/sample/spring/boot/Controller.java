package com.zzsong.job.sample.spring.boot;

import com.zzsong.job.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/8
 */
@RestController
public class Controller {
  private static final Logger log = LoggerFactory.getLogger(Controller.class);

  @PostMapping("/testHttpScript")
  public Map<String, Object> testHttpScript(@RequestBody Map<String, Object> param) {
    String jsonString = JsonUtils.toJsonString(param, true, true);
    log.info("testHttpScript 接收到请求参数: \n{}", jsonString);
    param.put("result", "success");
    return param;
  }
}
