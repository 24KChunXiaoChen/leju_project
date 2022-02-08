package top.leju.homefurnishing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("top.leju.homefurnishing.mapper")
public class HomeFurnishingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeFurnishingApplication.class, args);
    }

}

