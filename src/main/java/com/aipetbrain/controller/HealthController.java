package com.aipetbrain.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于微信云托管的健康检查探针
 */
@RestController
public class HealthController {

    /**
     * 健康检查端点
     * @return 返回 OK 状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "AIPetBrain service is running");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 简化的健康检查端点（仅返回 200 OK）
     * @return 返回 OK 状态
     */
    @GetMapping("/api/health")
    public Map<String, String> apiHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return response;
    }

    /**
     * 就绪检查端点
     * @return 返回 OK 状态
     */
    @GetMapping("/ready")
    public Map<String, String> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        return response;
    }
}

