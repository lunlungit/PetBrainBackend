package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@TableName("walk_record")
public class WalkRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long petId;
    private LocalDate walkDate;
    private Integer steps;        // 步数
    private BigDecimal distance; // 距离(km)
    private Integer duration;    // 时长(分钟)
    private Integer markCount;   // 标记次数
    private String routePath;    // 路径点(JSON格式)

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

