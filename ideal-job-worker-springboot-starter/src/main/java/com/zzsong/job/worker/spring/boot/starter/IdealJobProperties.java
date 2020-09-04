package com.zzsong.job.worker.spring.boot.starter;

import com.zzsong.job.worker.socket.ProtocolTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 配置信息
 *
 * @author 宋志宗
 * @date 2020/8/21
 */
@SuppressWarnings("SpellCheckingInspection")
@Getter
@Setter
@ConfigurationProperties("ideal.job")
public class IdealJobProperties {
    /**
     * 数据传输协议,默认rsocket
     */
    private ProtocolTypeEnum protocolType = ProtocolTypeEnum.RSOCKET;
    /**
     * 调度器地址列表:
     * <pre>
     *  websocket: ws://127.0.0.1:8804,ws://127.0.0.1:8805,ws://127.0.0.1:8806
     *  rsocket: 127.0.0.1:9904,127.0.0.1:9905,127.0.0.1:9906
     *      roscket端口号为调度器配置的rsocket端口, 而不是http端口
     * </pre>
     */
    private String schedulerAddresses = "";

    /**
     * 调度器验证token
     */
    private String accessToken = "";

    /**
     * 服务权重
     */
    private int weight = 1;

    /**
     * 服务应用名称, 如果为空则自动读取 spring.application.name
     */
    private String appName = "";
    /**
     * 当前服务的ip地址, 为空则会尝试自动获取
     */
    private String ip = "";
    /**
     * 当前服务的端口号, 小于1则会尝试自动获取
     */
    private int port = -1;

    @NestedConfigurationProperty
    private ThreadPoolProperties executorPool = new ThreadPoolProperties();
}
