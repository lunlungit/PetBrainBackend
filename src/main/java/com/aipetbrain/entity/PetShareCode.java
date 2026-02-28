package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宠物分享码实体
 * 用于微信分享的二维码和链接
 */
@Data
@TableName("pet_share_code")
public class PetShareCode {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 生成分享码的用户ID（宠物所有者）
     */
    private Long userId;

    /**
     * 分享码（唯一的短码，用于生成二维码）
     * 格式: SHARE_${petId}_${随机码}
     */
    private String shareCode;

    /**
     * 分享链接
     * 格式: https://app.example.com/share/${shareCode}
     */
    private String shareUrl;

    /**
     * 权限级别 READ:查看 WRITE:编辑
     */
    private String permission;

    /**
     * 过期时间（null表示永不过期）
     */
    private LocalDateTime expireTime;

    /**
     * 剩余使用次数（null表示无限制）
     */
    private Integer remainingUses;

    /**
     * 已被使用的次数
     */
    private Integer usedCount;

    /**
     * 是否激活
     */
    private Integer active;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;
}

