package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;
    private Long userId;
    private String hospital;
    private String doctor;
    private String diagnosis;
    private String symptoms;
    private String prescription;
    private BigDecimal cost;
    private LocalDateTime visitDate;

    // 附件: 病历照片、X光片、化验单
    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

