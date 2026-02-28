package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物体重记录表
 */
@Data
@TableName("weight_record")
public class WeightRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    /**
     * 记录日期(yyyy-MM-dd)
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDate recordDate;

    /**
     * 记录月份(1-12)
     */
    private Integer recordMonth;

    /**
     * 记录年份(YYYY)
     */
    private Integer recordYear;

    /**
     * 备注
     */
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记 0:未删除 1:已删除
     */
    @TableLogic
    private Integer deleted;
}

