package cn.sh.ideal.job.sample.spring.boot;

import cn.sh.ideal.job.common.message.payload.LoginMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import io.rsocket.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/7/14
 */
@RestController
@SpringBootApplication
public class ExecutorApplication {
  private static final String PATHPATTERN_ROUTEMATCHER_CLASS = "org.springframework.web.util.pattern.PathPatternRouteMatcher";
  private static final Logger log = LoggerFactory.getLogger(ExecutorApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ExecutorApplication.class, args);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final List<RSocketStrategiesCustomizer> customizers = new ArrayList<RSocketStrategiesCustomizer>() {
    {
      MediaType[] SUPPORTED_TYPES = {MediaType.APPLICATION_CBOR};
      add((strategy) -> {
        ObjectMapper mapper = new Jackson2ObjectMapperBuilder()
            .createXmlMapper(false).factory(new CBORFactory()).build();
        strategy.decoder(new Jackson2CborDecoder(mapper, SUPPORTED_TYPES));
        strategy.encoder(new Jackson2CborEncoder(mapper, SUPPORTED_TYPES));
      });
      add((strategy) -> {
        strategy.decoder(new Jackson2JsonDecoder(objectMapper, SUPPORTED_TYPES));
        strategy.encoder(new Jackson2JsonEncoder(objectMapper, SUPPORTED_TYPES));
      });
    }
  };

  private static final RSocketStrategies rsocketStrategies = rSocketStrategies();
  private static final RSocketRequester.Builder rsocketRequesterBuilder
      = RSocketRequester.builder().rsocketStrategies(rsocketStrategies);

  public static RSocketStrategies rSocketStrategies() {
    RSocketStrategies.Builder builder = RSocketStrategies.builder();
    if (ClassUtils.isPresent(PATHPATTERN_ROUTEMATCHER_CLASS, null)) {
      builder.routeMatcher(new PathPatternRouteMatcher());
    }
    customizers.forEach((customizer) -> customizer.customize(builder));
    return builder.build();
  }


  RSocketRequester rsocketRequester;


  @GetMapping
  public void test() throws JsonProcessingException {
    SocketAcceptor responder = RSocketMessageHandler.responder(rsocketStrategies, new ClientHandler());
    LoginMessage loginMessage = new LoginMessage();
    loginMessage.setAppName("example");
    loginMessage.setInstanceId("192.168.1.181");
    loginMessage.setAccessToken("jhsdagfahsdgfaksjhfkj");
    loginMessage.setWeight(10);
    this.rsocketRequester = rsocketRequesterBuilder
        .setupRoute("login")
        .setupData(objectMapper.writeValueAsString(loginMessage))
        .rsocketConnector(connector -> connector.acceptor(responder))
        .connectTcp("localhost", 9904)
        .doOnError(throwable -> log.info("", throwable))
        .block();

    this.rsocketRequester.rsocket()
        .onClose()
        .doOnError(error -> log.warn("Connection CLOSED", error))
        .doFinally(consumer -> log.info("Client DISCONNECTED\n"))
        .subscribe();
  }

}

class ClientHandler {
  private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

  @MessageMapping("client-status")
  public Flux<String> statusUpdate(String status) {
    log.info("Connection {}", status);
    return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
  }


  @MessageMapping("client-receive")
  public Mono<Void> receive(String message) {
    System.out.println(message);
//    client.rsocketRequester.rsocket().dispose();
    return Mono.empty();
  }
}