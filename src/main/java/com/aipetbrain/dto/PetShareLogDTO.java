package com.aipetbrain.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物权限分享记录 DTO
 */
@Data
public class PetShareLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long petId;

    /**
     * 分享者用户 ID
     */
    private Long fromUserId;

    /**
     * 分享者用户名
     */
    private String fromUserName;

    /**
     * 被分享者用户 ID
     */
    private Long toUserId;

    /**
     * 被分享者用户名
     */
    private String toUserName;

    /**
     * 操作类型：share(分享)，revoke(撤销)，update(更新权限)
     */
    private String action;

    /**
     * 操作类型描述
     */
    private String actionDesc;

    /**
     * 权限详情（JSON 格式）
     */
    private String permission;

    private LocalDateTime createTime;
}

