package com.aipetbrain.service.impl;

import com.aipetbrain.entity.FoodQuery;
import com.aipetbrain.mapper.FoodQueryMapper;
import com.aipetbrain.service.FoodQueryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodQueryServiceImpl implements FoodQueryService {

    private final FoodQueryMapper foodQueryMapper;

    @Override
    public List<FoodQuery> searchFood(String keyword, Integer petType) {
        LambdaQueryWrapper<FoodQuery> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(FoodQuery::getFoodName, keyword);
        wrapper.eq(FoodQuery::getPetType, petType);
        return foodQueryMapper.selectList(wrapper);
    }

    @Override
    public FoodQuery getFoodDetail(Long id) {
        return foodQueryMapper.selectById(id);
    }
}

