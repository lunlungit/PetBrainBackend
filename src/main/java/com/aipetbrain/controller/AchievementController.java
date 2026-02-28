package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.entity.Achievement;
import com.aipetbrain.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 勋章管理控制器
 */
@RestController
@RequestMapping("/achievement")
@RequiredArgsConstructor
@CrossOrigin
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * 获取用户的所有勋章（包括已解锁和待解锁）
     * @param userId 用户ID
     * @return 勋章列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserAchievements(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> achievements = achievementService.getUserAchievements(userId);
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(500, "获取勋章列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户已解锁的勋章
     * @param userId 用户ID
     * @return 已解锁的勋章列表
     */
    @GetMapping("/user/{userId}/unlocked")
    public Result<List<Achievement>> getUnlockedAchievements(@PathVariable Long userId) {
        try {
            List<Achievement> achievements = achievementService.getUnlockedAchievements(userId);
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(500, "获取已解锁勋章失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户已解锁勋章的数量
     * @param userId 用户ID
     * @return 数量
     */
    @GetMapping("/user/{userId}/unlocked-count")
    public Result<Map<String, Long>> getUnlockedCount(@PathVariable Long userId) {
        try {
            long count = achievementService.getUnlockedCount(userId);
            return Result.success(Map.of("count", count));
        } catch (Exception e) {
            return Result.error(500, "获取已解锁勋章数量失败: " + e.getMessage());
        }
    }

    /**
     * 手动检查并解锁用户应获得的勋章
     * @param userId 用户ID
     * @return 新解锁的勋章列表
     */
    @PostMapping("/user/{userId}/check-unlock")
    public Result<List<Achievement>> checkAndUnlockAchievements(@PathVariable Long userId) {
        try {
            List<Achievement> newUnlocked = achievementService.checkAndUnlockAchievements(userId);
            return Result.success(newUnlocked);
        } catch (Exception e) {
            return Result.error(500, "检查勋章解锁失败: " + e.getMessage());
        }
    }
}

