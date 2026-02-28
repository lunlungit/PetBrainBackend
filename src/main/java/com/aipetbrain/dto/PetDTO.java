package com.aipetbrain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PetDTO {
    private Long id;
    private Long userId;
    private Long creatorId;  // 宠物创建者ID
    private String name;
    private String avatar;
    private Integer type;
    private String breed;
    private LocalDate birthday;
    private BigDecimal weight;
    private Integer gender;
    private Integer sterilized;
    private String color;
    private Integer status;
    private Double distance;  // 距离（米）

    // 权限相关字段
    private Boolean canEdit;  // 当前用户是否可以编辑

    // 幂等性控制
    private transient String requestId;         // 用于幂等性控制，不持久化
}

