package com.aipetbrain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Hugging Face AI配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "huggingface")
public class HuggingFaceConfig {

    /**
     * Hugging Face API Token
     * 在 https://huggingface.co/settings/tokens 生成
     */
    private String apiToken;

    /**
     * 是否启用真实AI（true=真实AI，false=模拟数据）
     */
    private Boolean enabled = false;

    /**
     * 宠物识别模型（Google ViT / CLIP 等）
     */
    private String petModel = "google/vit-base-patch16-224";

    /**
     * 通用物体识别模型
     */
    private String objectModel = "openai/clip-vit-base-patch32";
}

