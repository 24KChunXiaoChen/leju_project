package top.leju.homefurnishing.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * socket配置类
 */
@Configuration
@ConfigurationProperties(prefix = "socket-config")
@Getter
@Setter
public class SocketConfig {
    private int port = 8888;//监听端口
    private int timeOut = 60*1000;//监听超时时间
}