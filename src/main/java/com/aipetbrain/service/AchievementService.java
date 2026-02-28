package com.aipetbrain.service;

import com.aipetbrain.entity.Achievement;
import java.util.List;
import java.util.Map;

/**
 * 勋章服务接口
 */
public interface AchievementService {
    /**
     * 获取用户的所有勋章（包括已解锁和待解锁）
     * @param userId 用户ID
     * @return 勋章列表（包含进度信息）
     */
    List<Map<String, Object>> getUserAchievements(Long userId);

    /**
     * 获取用户已解锁的勋章
     * @param userId 用户ID
     * @return 已解锁勋章列表
     */
    List<Achievement> getUnlockedAchievements(Long userId);

    /**
     * 获取用户已解锁勋章的数量
     * @param userId 用户ID
     * @return 数量
     */
    long getUnlockedCount(Long userId);

    /**
     * 检查并自动解锁用户应获得的勋章
     * @param userId 用户ID
     * @return 新解锁的勋章列表
     */
    List<Achievement> checkAndUnlockAchievements(Long userId);

    /**
     * 解锁单个勋章
     * @param userId 用户ID
     * @param achievementType 勋章类型
     */
    void unlockAchievement(Long userId, String achievementType);
}

