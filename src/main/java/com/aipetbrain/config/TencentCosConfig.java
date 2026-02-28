package com.aipetbrain.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 配置
 */
@Configuration
public class TencentCosConfig {

    @Data
    @ConfigurationProperties(prefix = "cos")
    public static class CosProperties {
        private boolean enabled;
        private String region;
        private String bucket;
        private String secretId;
        private String secretKey;
        private String cdnUrl;
    }

    @Bean
    @ConfigurationProperties(prefix = "cos")
    public CosProperties cosProperties() {
        return new CosProperties();
    }

    @Bean
    public COSClient cosClient(CosProperties cosProperties) {
        if (!cosProperties.isEnabled()) {
            return null;
        }

        // 初始化用户身份信息
        COSCredentials cred = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());

        // 设置bucket的地域
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));

        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}

