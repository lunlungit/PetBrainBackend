package com.aipetbrain.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重记录 DTO
 */
@Data
public class WeightRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long petId;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    /**
     * 记录日期(yyyy-MM-dd)
     */
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

