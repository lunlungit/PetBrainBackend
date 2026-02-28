package com.aipetbrain.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-宠物关联 DTO
 */
@Data
public class UserPetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long petId;

    /**
     * 角色：1=拥有者，2=共享用户
     */
    private Integer role;

    /**
     * 权限（JSON 格式）
     */
    private String permission;

    /**
     * 用户信息（用户名、头像等，可选）
     */
    private String userName;

    private String userAvatar;

    /**
     * 是否为拥有者
     */
    private Boolean isOwner;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

