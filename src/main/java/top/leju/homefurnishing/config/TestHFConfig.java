package top.leju.homefurnishing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;


@Configuration
@ConfigurationProperties(prefix = "test")
@Data
public class TestHFConfig {
    String tset = "这是默认值";
}
