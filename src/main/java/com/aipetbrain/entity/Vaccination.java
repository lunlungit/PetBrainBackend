package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vaccination")
public class Vaccination {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;
    private String vaccineName;
    private LocalDate vaccineDate;
    private LocalDate nextVaccineDate;
    private String hospital;
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

