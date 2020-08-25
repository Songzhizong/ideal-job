//package cn.sh.ideal.job.executor.core;
//
//import cn.sh.ideal.job.common.executor.RemoteJobExecutor;
//import cn.sh.ideal.job.common.message.payload.ExecuteJobCallback;
//import cn.sh.ideal.job.common.message.payload.ExecuteJobParam;
//import cn.sh.ideal.job.common.message.payload.IdleBeatCallback;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.drafts.Draft_6455;
//import org.java_websocket.handshake.ServerHandshake;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.net.URI;
//import java.net.URISyntaxException;
//
///**
// * @author 宋志宗
// * @date 2020/8/25
// */
//public class WebSocketRemoteJobExecutor2 extends Thread implements RemoteJobExecutor {
//  private static final Logger log = LoggerFactory.getLogger(WebSocketRemoteJobExecutor2.class);
//  /**
//   * 调度器程序地址
//   */
//  private String schedulerAddress;
//  /**
//   * 应用名称
//   */
//  private String appName;
//  /**
//   * 调度器ip地址
//   */
//  private String ip;
//  /**
//   * 调度器端口号
//   */
//  private int port;
//  private int weight = 1;
//  private String accessToken;
//  private int connectTimeOut = 200;
//  private long writeTimeOut = 200;
//  private long readTimeOut = 20000;
//  private boolean running = false;
//  private volatile boolean destroyed = false;
//
//  public synchronized void startSocket() {
//    if (destroyed) {
//      log.info("WebSocketRemoteJobExecutor is destroyed, schedulerAddress: {}", schedulerAddress);
//      return;
//    }
//    if (running) {
//      log.info("WebSocketRemoteJobExecutor is started, schedulerAddress: {}", schedulerAddress);
//      return;
//    }
//    running = true;
//    String address = schedulerAddress + "/websocket/executor/" +
//        appName + "/" + ip + ":" + port;
//    final URI uri;
//    try {
//      uri = new URI(address);
//    } catch (URISyntaxException e) {
//      log.error("{} URISyntaxException: {}", address, e.getMessage());
//      return;
//    }
//    final WebSocketClient socketClient = new WebSocketClient(uri, new Draft_6455()) {
//
//      @Override
//      public void onOpen(ServerHandshake handshakedata) {
//
//      }
//
//      @Override
//      public void onMessage(String message) {
//
//      }
//
//      @Override
//      public void onClose(int code, String reason, boolean remote) {
//
//      }
//
//      @Override
//      public void onError(Exception ex) {
//
//      }
//    };
//    socketClient.sendPing();
//
//  }
//
//
//  @Override
//  public void executeJobCallback(@Nonnull ExecuteJobCallback callback) {
//
//  }
//
//  @Override
//  public void idleBeatCallback(@Nonnull IdleBeatCallback callback) {
//
//  }
//
//  @Override
//  public void executeJob(@Nonnull ExecuteJobParam param) {
//
//  }
//
//  @Nonnull
//  @Override
//  public String getInstanceId() {
//    return null;
//  }
//
//  @Override
//  public boolean heartbeat() {
//    return false;
//  }
//
//  @Override
//  public int idleBeat(@Nullable Object key) {
//    return 0;
//  }
//
//  @Override
//  public void destroy() {
//
//  }
//}
