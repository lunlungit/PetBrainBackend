package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.dto.AIAnalyzeDTO;
import com.aipetbrain.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin
public class AIController {

    private final AIService aiService;

    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyze(@RequestBody AIAnalyzeDTO dto) {
        Map<String, Object> result = aiService.analyze(dto);
        return Result.success(result);
    }
}

