package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("territory")
public class Territory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long petId;
    private String locationName;  // 地点名称：如"朝阳公园"
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer markCount;    // 标记次数
    private LocalDateTime lastMarkTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

