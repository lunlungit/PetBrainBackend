package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("food_query")
public class FoodQuery {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String foodName;
    private Integer result;     // 0:可以吃 1:少吃 2:不能吃
    private String description;
    private String nutrition;    // 营养价值
    private Integer petType;     // 1:狗 2:猫

    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    @TableLogic
    private Integer deleted;
}

