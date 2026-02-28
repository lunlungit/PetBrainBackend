package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.dto.WeightRecordDTO;
import com.aipetbrain.service.WeightRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 体重记录 Controller
 */
@RestController
@RequestMapping("/weight-record")
@RequiredArgsConstructor
@CrossOrigin
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    /**
     * 添加或更新体重记录
     */
    @PostMapping("/add-or-update")
    public Result<WeightRecordDTO> addOrUpdateWeightRecord(@RequestBody WeightRecordDTO weightRecordDTO) {
        WeightRecordDTO result = weightRecordService.addOrUpdateWeightRecord(weightRecordDTO);
        return Result.success(result);
    }

    /**
     * 获取宠物某年某月的体重记录
     */
    @GetMapping("/get")
    public Result<WeightRecordDTO> getWeightRecord(
            @RequestParam Long petId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        WeightRecordDTO result = weightRecordService.getWeightRecord(petId, year, month);
        return Result.success(result);
    }

    /**
     * 获取宠物某年的所有体重记录
     */
    @GetMapping("/by-year")
    public Result<List<WeightRecordDTO>> getWeightRecordsByYear(
            @RequestParam Long petId,
            @RequestParam Integer year) {
        List<WeightRecordDTO> result = weightRecordService.getWeightRecordsByYear(petId, year);
        return Result.success(result);
    }

    /**
     * 获取宠物最近一年的体重记录
     */
    @GetMapping("/recent/{petId}")
    public Result<List<WeightRecordDTO>> getRecentWeightRecords(@PathVariable Long petId) {
        List<WeightRecordDTO> result = weightRecordService.getRecentWeightRecords(petId);
        return Result.success(result);
    }

    /**
     * 获取宠物所有体重记录
     */
    @GetMapping("/all/{petId}")
    public Result<List<WeightRecordDTO>> getAllWeightRecords(@PathVariable Long petId) {
        List<WeightRecordDTO> result = weightRecordService.getAllWeightRecords(petId);
        return Result.success(result);
    }

    /**
     * 删除体重记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteWeightRecord(@PathVariable Long id) {
        weightRecordService.deleteWeightRecord(id);
        return Result.success();
    }

    /**
     * 删除宠物某年的所有体重记录
     */
    @DeleteMapping("/delete-by-year")
    public Result<Void> deleteWeightRecordsByYear(
            @RequestParam Long petId,
            @RequestParam Integer year) {
        weightRecordService.deleteWeightRecordsByYear(petId, year);
        return Result.success();
    }
}

