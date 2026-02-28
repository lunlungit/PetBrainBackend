package com.aipetbrain.service;

import com.aipetbrain.dto.WeightRecordDTO;
import java.util.List;

/**
 * 体重记录服务接口
 */
public interface WeightRecordService {

    /**
     * 添加或更新体重记录
     *
     * @param weightRecordDTO 体重记录 DTO
     * @return 体重记录 DTO
     */
    WeightRecordDTO addOrUpdateWeightRecord(WeightRecordDTO weightRecordDTO);

    /**
     * 获取宠物的某年某月的体重记录
     *
     * @param petId 宠物ID
     * @param year 年份
     * @param month 月份（1-12）
     * @return 体重记录 DTO，如果不存在返回 null
     */
    WeightRecordDTO getWeightRecord(Long petId, Integer year, Integer month);

    /**
     * 获取宠物某年的所有体重记录
     *
     * @param petId 宠物ID
     * @param year 年份
     * @return 体重记录列表
     */
    List<WeightRecordDTO> getWeightRecordsByYear(Long petId, Integer year);

    /**
     * 获取宠物最近一年的体重记录
     *
     * @param petId 宠物ID
     * @return 体重记录列表
     */
    List<WeightRecordDTO> getRecentWeightRecords(Long petId);

    /**
     * 获取宠物所有体重记录
     *
     * @param petId 宠物ID
     * @return 体重记录列表
     */
    List<WeightRecordDTO> getAllWeightRecords(Long petId);

    /**
     * 删除体重记录
     *
     * @param id 记录ID
     */
    void deleteWeightRecord(Long id);

    /**
     * 删除宠物的某年数据
     *
     * @param petId 宠物ID
     * @param year 年份
     */
    void deleteWeightRecordsByYear(Long petId, Integer year);
}

