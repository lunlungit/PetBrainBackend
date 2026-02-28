package com.aipetbrain.service;

import com.aipetbrain.dto.WeightRecordDTO;
import com.aipetbrain.entity.WeightRecord;
import com.aipetbrain.mapper.WeightRecordMapper;
import com.aipetbrain.service.impl.WeightRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 体重记录服务测试
 */
@DisplayName("体重记录服务")
public class WeightRecordServiceTest {

    @Mock
    private WeightRecordMapper weightRecordMapper;

    @InjectMocks
    private WeightRecordServiceImpl weightRecordService;

    private WeightRecord testRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testRecord = new WeightRecord();
        testRecord.setId(1L);
        testRecord.setPetId(1L);
        testRecord.setWeight(BigDecimal.valueOf(8.5));
        testRecord.setRecordDate(LocalDate.of(2026, 2, 16));
        testRecord.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("添加体重记录成功")
    void testAddWeightRecordSuccess() {
        WeightRecordDTO dto = new WeightRecordDTO();
        dto.setPetId(1L);
        dto.setWeight(BigDecimal.valueOf(8.5));
        dto.setRecordDate(LocalDate.of(2026, 2, 16));

        when(weightRecordMapper.selectOne(any())).thenReturn(null);
        when(weightRecordMapper.insert(any())).thenReturn(1);

        WeightRecordDTO result = weightRecordService.addOrUpdateWeightRecord(dto);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals(BigDecimal.valueOf(8.5), result.getWeight());
    }

    @Test
    @DisplayName("更新体重记录成功")
    void testUpdateWeightRecordSuccess() {
        WeightRecordDTO dto = new WeightRecordDTO();
        dto.setId(1L);
        dto.setPetId(1L);
        dto.setWeight(BigDecimal.valueOf(9.0));
        dto.setRecordDate(LocalDate.of(2026, 2, 16));

        when(weightRecordMapper.selectOne(any())).thenReturn(testRecord);
        when(weightRecordMapper.updateById(any())).thenReturn(1);

        WeightRecordDTO result = weightRecordService.addOrUpdateWeightRecord(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(9.0), result.getWeight());
    }

    @Test
    @DisplayName("获取指定年份的体重记录")
    void testGetWeightRecordsByYear() {
        List<WeightRecord> records = new ArrayList<>();
        records.add(testRecord);

        when(weightRecordMapper.selectList(any())).thenReturn(records);

        List<WeightRecordDTO> result = weightRecordService.getWeightRecordsByYear(1L, 2026);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(8.5), result.get(0).getWeight());
    }

    @Test
    @DisplayName("获取指定年月的体重记录")
    void testGetWeightRecord() {
        when(weightRecordMapper.selectOne(any())).thenReturn(testRecord);

        WeightRecordDTO result = weightRecordService.getWeightRecord(1L, 2026, 2);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals(BigDecimal.valueOf(8.5), result.getWeight());
    }

    @Test
    @DisplayName("获取最近一年的体重记录")
    void testGetRecentWeightRecords() {
        List<WeightRecord> records = new ArrayList<>();
        records.add(testRecord);

        when(weightRecordMapper.selectList(any())).thenReturn(records);

        List<WeightRecordDTO> result = weightRecordService.getRecentWeightRecords(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("删除单条体重记录")
    void testDeleteRecord() {
        when(weightRecordMapper.updateById(any())).thenReturn(1);

        weightRecordService.deleteWeightRecord(1L);

        verify(weightRecordMapper, times(1)).updateById(any());
    }

    @Test
    @DisplayName("删除指定年份的所有体重记录")
    void testDeleteWeightRecordsByYear() {
        when(weightRecordMapper.update(any(), any())).thenReturn(12);

        weightRecordService.deleteWeightRecordsByYear(1L, 2026);

        verify(weightRecordMapper, times(1)).update(any(), any());
    }

    @Test
    @DisplayName("体重统计计算")
    void testWeightStatistics() {
        List<WeightRecord> records = new ArrayList<>();

        WeightRecord r1 = new WeightRecord();
        r1.setWeight(BigDecimal.valueOf(8.0));

        WeightRecord r2 = new WeightRecord();
        r2.setWeight(BigDecimal.valueOf(9.0));

        WeightRecord r3 = new WeightRecord();
        r3.setWeight(BigDecimal.valueOf(8.5));

        records.add(r1);
        records.add(r2);
        records.add(r3);

        when(weightRecordMapper.selectList(any())).thenReturn(records);

        List<WeightRecordDTO> result = weightRecordService.getWeightRecordsByYear(1L, 2026);

        // 验证可以计算统计数据
        BigDecimal max = result.stream()
            .map(WeightRecordDTO::getWeight)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal min = result.stream()
            .map(WeightRecordDTO::getWeight)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        assertEquals(BigDecimal.valueOf(9.0), max);
        assertEquals(BigDecimal.valueOf(8.0), min);
    }

    @Test
    @DisplayName("年份记录不存在时处理")
    void testNoRecordsForYear() {
        when(weightRecordMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<WeightRecordDTO> result = weightRecordService.getWeightRecordsByYear(1L, 2026);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

