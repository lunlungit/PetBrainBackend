package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.entity.MedicalRecord;
import com.aipetbrain.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
@CrossOrigin
public class MedicalController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/records/{petId}")
    public Result<List<MedicalRecord>> getMedicalRecords(@PathVariable Long petId) {
        List<MedicalRecord> records = medicalRecordService.getMedicalRecords(petId);
        return Result.success(records);
    }

    @PostMapping("/record/add")
    public Result<MedicalRecord> addMedicalRecord(@RequestBody MedicalRecord record) {
        MedicalRecord saved = medicalRecordService.addMedicalRecord(record);
        return Result.success(saved);
    }

    @PutMapping("/record/update")
    public Result<MedicalRecord> updateMedicalRecord(@RequestBody MedicalRecord record) {
        MedicalRecord updated = medicalRecordService.updateMedicalRecord(record);
        return Result.success(updated);
    }

    @DeleteMapping("/record/delete/{recordId}")
    public Result<Void> deleteMedicalRecord(@PathVariable Long recordId) {
        medicalRecordService.deleteMedicalRecord(recordId);
        return Result.success();
    }
}

