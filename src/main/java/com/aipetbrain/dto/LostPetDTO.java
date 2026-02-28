package com.aipetbrain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LostPetDTO {
    private Long id;
    private Long petId;
    private Long creatorId;
    private Integer petType;  // 1:狗 2:猫 3:其他
    private Integer isStray;  // 0:用户发布的走失宠物 1:用户发现的流浪宠物

    private String petName;
    private String breed;
    private String color;
    private String avatar;  // 主图
    private String images;  // 多张照片URL（JSON数组格式）
    private List<String> imageList;  // 照片列表（便于前端使用）
    private String description;

    private LocalDateTime lostTime;
    private String lostLocationName;
    private BigDecimal lostLatitude;
    private BigDecimal lostLongitude;

    private String contactName;
    private String contactPhone;
    private String contactWechat;

    private Integer status;  // 0:走失中/流浪中 1:已找到
    private LocalDateTime foundTime;
    private String foundLocationName;

    private Integer healthStatus;  // 0:未知 1:健康 2:受伤 3:生病
    private String behaviorDescription;

    private Double distance;  // 距离（米），由前端计算

    private Boolean canEdit;  // 当前用户是否可以编辑

    private LocalDateTime createTime;  // 记录创建时间

    // 幂等性控制
    private transient String requestId;  // 用于幂等性控制，不持久化
}

