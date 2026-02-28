package com.aipetbrain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 带权限信息的宠物 DTO
 * 在 PetDTO 基础上添加权限和所有者信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetWithPermissionDTO extends PetDTO {

    /**
     * 是否为拥有者
     */
    private Boolean isOwner;

    /**
     * 用户对该宠物的权限列表
     */
    private List<String> permissions;

    /**
     * 宠物创建者 ID
     */
    private Long creatorId;

    /**
     * 宠物创建者用户名（如果不是自己创建）
     */
    private String creatorName;

    /**
     * 宠物创建者头像
     */
    private String creatorAvatar;
}

