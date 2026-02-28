package com.aipetbrain.dto;

import lombok.Data;

@Data
public class AIAnalyzeDTO {
    private String imageUrl;
    private Integer analyzeType; // 1:粑粑分析 2:皮肤分析 3:宠物识别 4:食物查询
    private Long petId;
    private String foodName; // 食物名称
    private Integer petType; // 宠物类型 1:狗 2:猫 3:其他
}

