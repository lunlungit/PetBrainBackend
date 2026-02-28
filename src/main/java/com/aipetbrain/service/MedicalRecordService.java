package com.aipetbrain.service;

import com.aipetbrain.entity.MedicalRecord;
import java.util.List;

public interface MedicalRecordService {
    List<MedicalRecord> getMedicalRecords(Long petId);
    MedicalRecord addMedicalRecord(MedicalRecord record);
    MedicalRecord updateMedicalRecord(MedicalRecord record);
    void deleteMedicalRecord(Long recordId);
}

