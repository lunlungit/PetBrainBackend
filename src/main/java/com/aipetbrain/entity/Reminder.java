package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("reminder")
public class Reminder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long petId;
    private Integer type;        // 1:喂食 2:遛狗 3:驱虫 4:洗澡 5:打疫苗 6:复查
    private String title;
    @JsonProperty("remindDate")
    private LocalDate remindDate;  // 提醒日期（支持未来一年的日期）
    private LocalTime remindTime;
    private Integer repeatType;  // 0:一次 1:每天 2:每周 3:每月
    private Integer status;       // 0:待完成 1:已完成
    private LocalDateTime lastTriggerTime;
    private LocalDateTime lastPushTime; // 上次推送时间，用于控制一天只推送一次

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

