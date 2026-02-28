package com.aipetbrain.service.impl;

import com.aipetbrain.dto.WeightRecordDTO;
import com.aipetbrain.entity.WeightRecord;
import com.aipetbrain.mapper.WeightRecordMapper;
import com.aipetbrain.service.WeightRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 体重记录服务实现
 */
@Service
@RequiredArgsConstructor
public class WeightRecordServiceImpl implements WeightRecordService {

    private final WeightRecordMapper weightRecordMapper;

    @Override
    public WeightRecordDTO addOrUpdateWeightRecord(WeightRecordDTO weightRecordDTO) {
        // 查找是否已存在该记录
        WeightRecord existing = weightRecordMapper.selectOne(
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, weightRecordDTO.getPetId())
                .eq(WeightRecord::getRecordDate, weightRecordDTO.getRecordDate())
                .eq(WeightRecord::getDeleted, 0)
        );

        WeightRecord record = new WeightRecord();
        BeanUtils.copyProperties(weightRecordDTO, record);

        if (existing != null) {
            // 更新现有记录
            record.setId(existing.getId());
            record.setUpdateTime(LocalDateTime.now());
            weightRecordMapper.updateById(record);
        } else {
            // 创建新记录
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            weightRecordMapper.insert(record);
        }

        // 返回更新后的 DTO
        WeightRecordDTO result = new WeightRecordDTO();
        BeanUtils.copyProperties(record, result);
        return result;
    }

    @Override
    public WeightRecordDTO getWeightRecord(Long petId, Integer year, Integer month) {
        // 构造查询日期范围（该月的第一天到该月的最后一天）
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        WeightRecord record = weightRecordMapper.selectOne(
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, petId)
                .ge(WeightRecord::getRecordDate, startDate)
                .le(WeightRecord::getRecordDate, endDate)
                .eq(WeightRecord::getDeleted, 0)
        );

        if (record == null) {
            return null;
        }

        WeightRecordDTO dto = new WeightRecordDTO();
        BeanUtils.copyProperties(record, dto);
        return dto;
    }

    @Override
    public List<WeightRecordDTO> getWeightRecordsByYear(Long petId, Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<WeightRecord> records = weightRecordMapper.selectList(
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, petId)
                .ge(WeightRecord::getRecordDate, startDate)
                .le(WeightRecord::getRecordDate, endDate)
                .eq(WeightRecord::getDeleted, 0)
                .orderByAsc(WeightRecord::getRecordDate)
        );

        return records.stream().map(record -> {
            WeightRecordDTO dto = new WeightRecordDTO();
            BeanUtils.copyProperties(record, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WeightRecordDTO> getRecentWeightRecords(Long petId) {
        // 获取最近一年的记录（当前年份往前推12个月）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        List<WeightRecord> records = weightRecordMapper.selectList(
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, petId)
                .ge(WeightRecord::getRecordDate, startDate)
                .le(WeightRecord::getRecordDate, endDate)
                .eq(WeightRecord::getDeleted, 0)
                .orderByDesc(WeightRecord::getRecordDate)
        );

        return records.stream().map(record -> {
            WeightRecordDTO dto = new WeightRecordDTO();
            BeanUtils.copyProperties(record, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WeightRecordDTO> getAllWeightRecords(Long petId) {
        // 获取宠物所有体重记录
        List<WeightRecord> records = weightRecordMapper.selectList(
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, petId)
                .eq(WeightRecord::getDeleted, 0)
                .orderByAsc(WeightRecord::getRecordDate)
        );

        return records.stream().map(record -> {
            WeightRecordDTO dto = new WeightRecordDTO();
            BeanUtils.copyProperties(record, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteWeightRecord(Long id) {
        WeightRecord record = new WeightRecord();
        record.setId(id);
        record.setDeleted(1);
        record.setUpdateTime(LocalDateTime.now());
        weightRecordMapper.updateById(record);
    }

    @Override
    public void deleteWeightRecordsByYear(Long petId, Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        WeightRecord record = new WeightRecord();
        record.setDeleted(1);
        record.setUpdateTime(LocalDateTime.now());

        weightRecordMapper.update(record,
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, petId)
                .ge(WeightRecord::getRecordDate, startDate)
                .le(WeightRecord::getRecordDate, endDate)
        );
    }
}

