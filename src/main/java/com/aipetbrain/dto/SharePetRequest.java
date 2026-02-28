package com.aipetbrain.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分享宠物请求 DTO
 */
@Data
public class SharePetRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被分享者用户 ID
     */
    private Long toUserId;

    /**
     * 权限列表，如：["view","edit"]
     */
    private List<String> permissions;
}

