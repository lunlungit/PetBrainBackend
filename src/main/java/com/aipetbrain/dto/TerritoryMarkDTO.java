package com.aipetbrain.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TerritoryMarkDTO {
    private Long userId;
    private Long petId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;
}

