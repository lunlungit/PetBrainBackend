package com.aipetbrain.service.impl;

import com.aipetbrain.dto.TerritoryMarkDTO;
import com.aipetbrain.entity.Territory;
import com.aipetbrain.entity.User;
import com.aipetbrain.entity.Pet;
import com.aipetbrain.mapper.TerritoryMapper;
import com.aipetbrain.mapper.UserMapper;
import com.aipetbrain.mapper.PetMapper;
import com.aipetbrain.service.TerritoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {

    private final TerritoryMapper territoryMapper;
    private final UserMapper userMapper;
    private final PetMapper petMapper;

    @Override
    public Territory markTerritory(TerritoryMarkDTO dto) {
        // 查找附近已有的标记
        LambdaQueryWrapper<Territory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Territory::getUserId, dto.getUserId());
        wrapper.eq(Territory::getPetId, dto.getPetId());

        Territory existing = territoryMapper.selectOne(wrapper);

        if (existing != null) {
            // 检查是否在10分钟冷却时间内
            LocalDateTime lastMarkTime = existing.getLastMarkTime();
            if (lastMarkTime != null) {
                LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
                if (lastMarkTime.isAfter(tenMinutesAgo)) {
                    throw new RuntimeException("10分钟内只能标记一次，请稍后再试");
                }
            }
            // 更新已有标记
            existing.setLatitude(dto.getLatitude());
            existing.setLongitude(dto.getLongitude());
            existing.setMarkCount(existing.getMarkCount() + 1);
            existing.setLastMarkTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());
            territoryMapper.updateById(existing);
            return existing;
        } else {
            // 创建新标记
            Territory territory = new Territory();
            territory.setUserId(dto.getUserId());
            territory.setPetId(dto.getPetId());
            territory.setLocationName(dto.getLocationName());
            territory.setLatitude(dto.getLatitude());
            territory.setLongitude(dto.getLongitude());
            territory.setMarkCount(1);
            territory.setLastMarkTime(LocalDateTime.now());
            territory.setCreateTime(LocalDateTime.now());
            territoryMapper.insert(territory);
            return territory;
        }
    }

    @Override
    public List<Map<String, Object>> getNearbyTerritories(BigDecimal latitude, BigDecimal longitude, Double radius) {
        // 简化处理：获取所有标记，实际应该计算距离筛选
        LambdaQueryWrapper<Territory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Territory::getMarkCount);
        wrapper.last("LIMIT 50");

        List<Territory> territories = territoryMapper.selectList(wrapper);

        // 返回包含宠物信息的Map列表
        return territories.stream().map(territory -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", territory.getId());
            map.put("userId", territory.getUserId());
            map.put("petId", territory.getPetId());
            map.put("locationName", territory.getLocationName());
            map.put("latitude", territory.getLatitude());
            map.put("longitude", territory.getLongitude());
            map.put("markCount", territory.getMarkCount());
            map.put("lastMarkTime", territory.getLastMarkTime());

            // 查询宠物信息
            Pet pet = petMapper.selectById(territory.getPetId());
            if (pet != null) {
                map.put("petName", pet.getName());
                map.put("petAvatar", pet.getAvatar());
            }

            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getLeaderboard(String type) {
        List<Territory> territories = territoryMapper.selectList(null);

        return territories.stream()
            .sorted((t1, t2) -> t2.getMarkCount().compareTo(t1.getMarkCount()))
            .limit(10)
            .map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", t.getUserId());
                map.put("petId", t.getPetId());
                map.put("markCount", t.getMarkCount());
                map.put("locationName", t.getLocationName());
                return map;
            })
            .collect(Collectors.toList());
    }
}

