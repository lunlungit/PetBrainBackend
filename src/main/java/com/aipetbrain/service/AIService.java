package com.aipetbrain.service;

import com.aipetbrain.dto.AIAnalyzeDTO;
import java.util.Map;

public interface AIService {
    Map<String, Object> analyze(AIAnalyzeDTO dto);
}

