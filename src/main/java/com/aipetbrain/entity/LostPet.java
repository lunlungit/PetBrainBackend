package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("lost_pet")
public class LostPet {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;  // 关联的宠物ID（如果是用户自己的宠物，否则为NULL）
    private Long creatorId;  // 发布者/上报者用户ID
    private Integer petType;  // 宠物类型 1:狗 2:猫 3:其他
    private Integer isStray;  // 是否流浪宠物 0:用户发布的走失宠物 1:用户发现的流浪宠物

    private String petName;  // 宠物名称
    private String breed;  // 品种描述
    private String color;  // 毛色/外观特征
    private String avatar;  // 宠物照片URL（主图）
    private String images;  // 宠物多张照片URL（JSON数组格式，如 ["url1","url2","url3"]）
    private String description;  // 详细描述

    // 走失/发现信息
    private LocalDateTime lostTime;  // 走失/发现时间
    private String lostLocationName;  // 走失/发现地点名称
    private BigDecimal lostLatitude;  // 走失/发现地点纬度
    private BigDecimal lostLongitude;  // 走失/发现地点经度

    // 联系信息
    private String contactName;  // 联系人名称
    private String contactPhone;  // 联系人电话
    private String contactWechat;  // 联系人微信号

    // 状态信息
    private Integer status;  // 状态 0:走失中/流浪中 1:已找到
    private LocalDateTime foundTime;  // 找到时间
    private String foundLocationName;  // 找到地点名称

    // 流浪宠物特定字段
    private Integer healthStatus;  // 健康状况 0:未知 1:健康 2:受伤 3:生病
    private String behaviorDescription;  // 行为描述（友好度、反应等）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

