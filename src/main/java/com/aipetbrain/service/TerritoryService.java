package com.aipetbrain.service;

import com.aipetbrain.dto.TerritoryMarkDTO;
import com.aipetbrain.entity.Territory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TerritoryService {
    Territory markTerritory(TerritoryMarkDTO dto);
    List<Map<String, Object>> getNearbyTerritories(BigDecimal latitude, BigDecimal longitude, Double radius);
    List<Map<String, Object>> getLeaderboard(String type);
}

