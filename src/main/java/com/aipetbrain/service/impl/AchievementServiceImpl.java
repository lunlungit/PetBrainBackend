package com.aipetbrain.service.impl;

import com.aipetbrain.entity.*;
import com.aipetbrain.mapper.*;
import com.aipetbrain.service.AchievementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 勋章服务实现类
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private TerritoryMapper territoryMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private ReminderMapper reminderMapper;

    /**
     * 获取用户的所有勋章（包括已解锁和待解锁）
     */
    @Override
    public List<Map<String, Object>> getUserAchievements(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 定义所有勋章类型和信息
        Map<String, Map<String, String>> achievementDefinitions = getAchievementDefinitions();

        for (Map.Entry<String, Map<String, String>> entry : achievementDefinitions.entrySet()) {
            String type = entry.getKey();
            Map<String, String> info = entry.getValue();

            Map<String, Object> achievement = new HashMap<>();
            achievement.put("type", type);
            achievement.put("name", info.get("name"));
            achievement.put("description", info.get("description"));
            achievement.put("icon", info.get("icon"));
            achievement.put("bgColor", info.get("bgColor"));

            // 检查是否已解锁
            LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Achievement::getUserId, userId)
                   .eq(Achievement::getType, type);
            Achievement unlocked = achievementMapper.selectOne(wrapper);

            if (unlocked != null) {
                achievement.put("locked", false);
                achievement.put("progress", 100);
                achievement.put("unlockTime", unlocked.getUnlockTime());
            } else {
                achievement.put("locked", true);
                // 计算进度
                int progress = calculateProgress(userId, type);
                achievement.put("progress", progress);
            }

            result.add(achievement);
        }

        return result;
    }

    /**
     * 获取用户已解锁的勋章
     */
    @Override
    public List<Achievement> getUnlockedAchievements(Long userId) {
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Achievement::getUserId, userId);
        return achievementMapper.selectList(wrapper);
    }

    /**
     * 获取用户已解锁勋章的数量
     */
    @Override
    public long getUnlockedCount(Long userId) {
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Achievement::getUserId, userId);
        return achievementMapper.selectCount(wrapper);
    }

    /**
     * 检查并自动解锁用户应获得的勋章
     */
    @Override
    public List<Achievement> checkAndUnlockAchievements(Long userId) {
        List<Achievement> newUnlocked = new ArrayList<>();

        // 获取所有勋章类型
        Map<String, Map<String, String>> achievementDefinitions = getAchievementDefinitions();

        for (String type : achievementDefinitions.keySet()) {
            // 检查该勋章是否已解锁
            LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Achievement::getUserId, userId)
                   .eq(Achievement::getType, type);
            Achievement existing = achievementMapper.selectOne(wrapper);

            if (existing != null) {
                continue; // 已解锁，跳过
            }

            // 检查是否应该解锁
            if (shouldUnlockAchievement(userId, type)) {
                Achievement achievement = new Achievement();
                achievement.setUserId(userId);
                achievement.setType(type);
                Map<String, String> info = achievementDefinitions.get(type);
                achievement.setName(info.get("name"));
                achievement.setDescription(info.get("description"));
                achievement.setIcon(info.get("icon"));
                achievement.setUnlockTime(LocalDateTime.now());

                achievementMapper.insert(achievement);
                newUnlocked.add(achievement);
            }
        }

        return newUnlocked;
    }

    /**
     * 解锁单个勋章
     */
    @Override
    public void unlockAchievement(Long userId, String achievementType) {
        // 检查是否已解锁
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Achievement::getUserId, userId)
               .eq(Achievement::getType, achievementType);
        Achievement existing = achievementMapper.selectOne(wrapper);

        if (existing != null) {
            return; // 已解锁
        }

        Map<String, String> info = getAchievementDefinitions().get(achievementType);
        if (info == null) {
            return;
        }

        Achievement achievement = new Achievement();
        achievement.setUserId(userId);
        achievement.setType(achievementType);
        achievement.setName(info.get("name"));
        achievement.setDescription(info.get("description"));
        achievement.setIcon(info.get("icon"));
        achievement.setUnlockTime(LocalDateTime.now());

        achievementMapper.insert(achievement);
    }

    /**
     * 勋章定义
     */
    private Map<String, Map<String, String>> getAchievementDefinitions() {
        Map<String, Map<String, String>> definitions = new LinkedHashMap<>();

        definitions.put("park_king", Map.of(
            "name", "公园之王",
            "description", "占领公园领地超过30天",
            "icon", "👑",
            "bgColor", "linear-gradient(135deg, #FFD93D 0%, #FF9F1C 100%)"
        ));

        definitions.put("night_walker", Map.of(
            "name", "夜行侠",
            "description", "夜间遛狗超过20次",
            "icon", "🌙",
            "bgColor", "linear-gradient(135deg, #7B61FF 0%, #5A3FD9 100%)"
        ));

        definitions.put("mark_maniac", Map.of(
            "name", "撒尿狂魔",
            "description", "标记领地超过50次",
            "icon", "💦",
            "bgColor", "linear-gradient(135deg, #00D4FF 0%, #0099CC 100%)"
        ));

        definitions.put("health_expert", Map.of(
            "name", "健康达人",
            "description", "连续记录宠物健康数据30天",
            "icon", "💪",
            "bgColor", "linear-gradient(135deg, #00FFA8 0%, #00CC88 100%)"
        ));

        definitions.put("finance_master", Map.of(
            "name", "理财高手",
            "description", "记账超过100笔",
            "icon", "💰",
            "bgColor", "linear-gradient(135deg, #FF6B9D 0%, #FF4785 100%)"
        ));

        definitions.put("early_bird", Map.of(
            "name", "早起鸟",
            "description", "早上7点前遛狗10次",
            "icon", "🐦",
            "bgColor", "linear-gradient(135deg, #A855F7 0%, #7C3AED 100%)"
        ));

        definitions.put("food_explorer", Map.of(
            "name", "美食家",
            "description", "尝试新食物查询超过50次",
            "icon", "🍽️",
            "bgColor", "linear-gradient(135deg, #F59E0B 0%, #D97706 100%)"
        ));

        definitions.put("social_master", Map.of(
            "name", "社交达人",
            "description", "遇到其他宠物主人20次",
            "icon", "🤝",
            "bgColor", "linear-gradient(135deg, #10B981 0%, #059669 100%)"
        ));

        return definitions;
    }

    /**
     * 检查是否应该解锁勋章
     */
    private boolean shouldUnlockAchievement(Long userId, String type) {
        return switch (type) {
            case "park_king" -> checkParkKing(userId);
            case "night_walker" -> checkNightWalker(userId);
            case "mark_maniac" -> checkMarkManiac(userId);
            case "health_expert" -> checkHealthExpert(userId);
            case "finance_master" -> checkFinanceMaster(userId);
            case "early_bird" -> checkEarlyBird(userId);
            case "food_explorer" -> checkFoodExplorer(userId);
            case "social_master" -> checkSocialMaster(userId);
            default -> false;
        };
    }

    /**
     * 计算勋章进度（用于前端展示）
     */
    private int calculateProgress(Long userId, String type) {
        return switch (type) {
            case "park_king" -> calculateParkKingProgress(userId);
            case "night_walker" -> calculateNightWalkerProgress(userId);
            case "mark_maniac" -> calculateMarkManiacProgress(userId);
            case "health_expert" -> calculateHealthExpertProgress(userId);
            case "finance_master" -> calculateFinanceMasterProgress(userId);
            case "early_bird" -> calculateEarlyBirdProgress(userId);
            case "food_explorer" -> calculateFoodExplorerProgress(userId);
            case "social_master" -> calculateSocialMasterProgress(userId);
            default -> 0;
        };
    }

    // ==================== 勋章检查方法 ====================

    /**
     * 公园之王：占领公园领地超过30天
     */
    private boolean checkParkKing(Long userId) {
        LambdaQueryWrapper<Territory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Territory::getUserId, userId);
        List<Territory> territories = territoryMapper.selectList(wrapper);

        for (Territory territory : territories) {
            if (territory.getCreateTime() != null &&
                territory.getCreateTime().plusDays(30).isBefore(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    private int calculateParkKingProgress(Long userId) {
        LambdaQueryWrapper<Territory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Territory::getUserId, userId);
        List<Territory> territories = territoryMapper.selectList(wrapper);

        if (territories.isEmpty()) return 0;

        long maxDays = 0;
        for (Territory territory : territories) {
            if (territory.getCreateTime() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                    territory.getCreateTime(), LocalDateTime.now()
                );
                maxDays = Math.max(maxDays, days);
            }
        }

        return Math.min(100, (int) (maxDays * 100 / 30));
    }

    /**
     * 夜行侠：夜间遛狗超过20次
     * (这个需要在宠物活动记录中记录，暂时以宠物数量作为简化版本)
     */
    private boolean checkNightWalker(Long userId) {
        // 获取用户的所有宠物
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId);
        long petCount = petMapper.selectCount(wrapper);

        return petCount >= 1; // 简化版：有宠物即可
    }

    private int calculateNightWalkerProgress(Long userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId);
        long petCount = petMapper.selectCount(wrapper);

        return Math.min(100, (int) (petCount * 100 / 20));
    }

    /**
     * 撒尿狂魔：标记领地超过50次
     */
    private boolean checkMarkManiac(Long userId) {
        LambdaQueryWrapper<Territory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Territory::getUserId, userId);
        long count = territoryMapper.selectCount(wrapper);

        return count >= 50;
    }

    private int calculateMarkManiacProgress(Long userId) {
        LambdaQueryWrapper<Territory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Territory::getUserId, userId);
        long count = territoryMapper.selectCount(wrapper);

        return Math.min(100, (int) (count * 100 / 50));
    }

    /**
     * 健康达人：连续记录宠物健康数据30天
     * (这个需要专门的连续天数记录，暂时以医疗记录数量作为简化版本)
     */
    private boolean checkHealthExpert(Long userId) {
        LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
        petWrapper.eq(Pet::getUserId, userId);
        List<Pet> pets = petMapper.selectList(petWrapper);

        for (Pet pet : pets) {
            LambdaQueryWrapper<MedicalRecord> medicalWrapper = new LambdaQueryWrapper<>();
            medicalWrapper.eq(MedicalRecord::getPetId, pet.getId());
            long count = medicalRecordMapper.selectCount(medicalWrapper);

            if (count >= 30) {
                return true;
            }
        }

        return false;
    }

    private int calculateHealthExpertProgress(Long userId) {
        LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
        petWrapper.eq(Pet::getUserId, userId);
        List<Pet> pets = petMapper.selectList(petWrapper);

        long maxCount = 0;
        for (Pet pet : pets) {
            LambdaQueryWrapper<MedicalRecord> medicalWrapper = new LambdaQueryWrapper<>();
            medicalWrapper.eq(MedicalRecord::getPetId, pet.getId());
            long count = medicalRecordMapper.selectCount(medicalWrapper);
            maxCount = Math.max(maxCount, count);
        }

        return Math.min(100, (int) (maxCount * 100 / 30));
    }

    /**
     * 理财高手：记账超过100笔
     * (这个需要记账表，暂时返回false)
     */
    private boolean checkFinanceMaster(Long userId) {
        // TODO: 需要记账表数据
        return false;
    }

    private int calculateFinanceMasterProgress(Long userId) {
        // TODO: 需要记账表数据
        return 0;
    }

    /**
     * 早起鸟：早上7点前遛狗10次
     * (这个需要遛狗打卡记录，暂时以宠物数量作为简化版本)
     */
    private boolean checkEarlyBird(Long userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId);
        long count = petMapper.selectCount(wrapper);

        return count >= 10;
    }

    private int calculateEarlyBirdProgress(Long userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId);
        long count = petMapper.selectCount(wrapper);

        return Math.min(100, (int) (count * 100 / 10));
    }

    /**
     * 美食家：尝试新食物查询超过50次
     * (这个需要食物查询记录，暂时返回false)
     */
    private boolean checkFoodExplorer(Long userId) {
        // TODO: 需要食物查询记录表
        return false;
    }

    private int calculateFoodExplorerProgress(Long userId) {
        // TODO: 需要食物查询记录表
        return 0;
    }

    /**
     * 社交达人：遇到其他宠物主人20次
     * (这个需要宠物社交记录，暂时返回false)
     */
    private boolean checkSocialMaster(Long userId) {
        // TODO: 需要社交记录表
        return false;
    }

    private int calculateSocialMasterProgress(Long userId) {
        // TODO: 需要社交记录表
        return 0;
    }
}

