package com.aipetbrain.service.impl;

import com.aipetbrain.entity.MedicalRecord;
import com.aipetbrain.mapper.MedicalRecordMapper;
import com.aipetbrain.service.MedicalRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    public List<MedicalRecord> getMedicalRecords(Long petId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPetId, petId);
        wrapper.orderByDesc(MedicalRecord::getVisitDate);
        return medicalRecordMapper.selectList(wrapper);
    }

    @Override
    public MedicalRecord addMedicalRecord(MedicalRecord record) {
        record.setCreateTime(LocalDateTime.now());
        medicalRecordMapper.insert(record);
        return record;
    }

    @Override
    public MedicalRecord updateMedicalRecord(MedicalRecord record) {
        record.setUpdateTime(LocalDateTime.now());
        medicalRecordMapper.updateById(record);
        return record;
    }

    @Override
    public void deleteMedicalRecord(Long recordId) {
        medicalRecordMapper.deleteById(recordId);
    }
}

