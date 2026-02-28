package com.aipetbrain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.aipetbrain.mapper")
@EnableAsync
@EnableScheduling
public class AIPetBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIPetBrainApplication.class, args);
        System.out.println("""

                ========================================
                  🐾 AIPetBrain 启动成功！
                  🚀 服务器运行在: http://localhost:8080/api
                  📱 宠物社区微信小程序后端服务已就绪
                ========================================
                """);
    }
}

