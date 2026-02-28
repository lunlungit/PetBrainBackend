package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;  // 保留以兼容旧代码，新代码应使用 creatorId 和 user_pet 表
    private Long creatorId;  // 宠物创建者 ID
    private String name;
    private String avatar;
    private Integer type;  // 1:狗 2:猫 3:其他
    private String breed;   // 品种
    private LocalDate birthday;
    private BigDecimal weight;  // 体重(kg)
    private Integer gender;     // 0:公 1:母
    private Integer sterilized; // 0:未绝育 1:已绝育
    private String color;
    private Integer status;     // 0:正常 1:生病 2:走失

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

