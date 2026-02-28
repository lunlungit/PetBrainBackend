package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.entity.FoodQuery;
import com.aipetbrain.service.FoodQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
@CrossOrigin
public class FoodQueryController {

    private final FoodQueryService foodQueryService;

    @GetMapping("/search")
    public Result<List<FoodQuery>> searchFood(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer petType) {
        List<FoodQuery> foods = foodQueryService.searchFood(keyword, petType);
        return Result.success(foods);
    }

    @GetMapping("/detail/{id}")
    public Result<FoodQuery> getFoodDetail(@PathVariable Long id) {
        FoodQuery food = foodQueryService.getFoodDetail(id);
        return Result.success(food);
    }
}

