package com.aipetbrain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宠物分享码DTO
 */
@Data
public class PetShareCodeDTO {
    private Long id;
    private Long petId;
    private Long userId;
    private String shareCode;
    private String shareUrl;
    private String permission;
    private LocalDateTime expireTime;
    private Integer remainingUses;
    private Integer usedCount;
    private Integer active;
    private LocalDateTime createTime;
}

