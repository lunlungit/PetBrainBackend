package com.aipetbrain.service;

import com.aipetbrain.entity.FoodQuery;
import java.util.List;

public interface FoodQueryService {
    List<FoodQuery> searchFood(String keyword, Integer petType);
    FoodQuery getFoodDetail(Long id);
}

