package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.dto.TerritoryMarkDTO;
import com.aipetbrain.entity.Territory;
import com.aipetbrain.service.TerritoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/territory")
@RequiredArgsConstructor
@CrossOrigin
public class TerritoryController {

    private final TerritoryService territoryService;

    @PostMapping("/mark")
    public Result<Territory> markTerritory(@RequestBody TerritoryMarkDTO dto) {
        Territory territory = territoryService.markTerritory(dto);
        return Result.success(territory);
    }

    @GetMapping("/nearby")
    public Result<List<Map<String, Object>>> getNearbyTerritories(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "1000") Double radius) {
        List<Map<String, Object>> territories = territoryService.getNearbyTerritories(latitude, longitude, radius);
        return Result.success(territories);
    }

    @GetMapping("/leaderboard")
    public Result<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "mark") String type) {
        List<Map<String, Object>> leaderboard = territoryService.getLeaderboard(type);
        return Result.success(leaderboard);
    }
}

