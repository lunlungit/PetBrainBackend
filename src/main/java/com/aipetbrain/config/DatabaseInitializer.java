package com.aipetbrain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化组件
 * 在 Spring Boot 应用启动后执行数据库迁移和初始化脚本
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initDatabase() {
        log.info("开始执行数据库初始化脚本...");

        try {
            // 确保 lost_pet 表存在
            ensureLostPetTableExists();
            log.info("✅ lost_pet 表检查完成");
        } catch (Exception e) {
            log.error("❌ 数据库初始化失败:", e);
        }
    }

    /**
     * 确保 lost_pet 表存在，如果不存在则创建
     */
    private void ensureLostPetTableExists() {
        try {
            if (!tableExists("lost_pet")) {
                log.info("创建 lost_pet 表...");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS `lost_pet` (" +
                        "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID'," +
                        "  `pet_id` BIGINT DEFAULT NULL COMMENT '关联的宠物ID'," +
                        "  `creator_id` BIGINT NOT NULL COMMENT '发布者/上报者用户ID'," +
                        "  `pet_type` TINYINT NOT NULL COMMENT '宠物类型 1:狗 2:猫 3:其他'," +
                        "  `is_stray` TINYINT DEFAULT 0 COMMENT '是否流浪宠物 0:用户发布的走失宠物 1:用户发现的流浪宠物'," +
                        "  `pet_name` VARCHAR(50) DEFAULT NULL COMMENT '宠物名称'," +
                        "  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种描述'," +
                        "  `color` VARCHAR(100) DEFAULT NULL COMMENT '毛色/外观特征'," +
                        "  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '宠物照片URL'," +
                        "  `description` TEXT COMMENT '详细描述'," +
                        "  `lost_time` DATETIME DEFAULT NULL COMMENT '走失/发现时间'," +
                        "  `lost_location_name` VARCHAR(100) DEFAULT NULL COMMENT '走失/发现地点名称'," +
                        "  `lost_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '走失/发现地点纬度'," +
                        "  `lost_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '走失/发现地点经度'," +
                        "  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人名称'," +
                        "  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话'," +
                        "  `contact_wechat` VARCHAR(50) DEFAULT NULL COMMENT '联系人微信号'," +
                        "  `status` TINYINT DEFAULT 0 COMMENT '状态 0:走失中/流浪中 1:已找到'," +
                        "  `found_time` DATETIME DEFAULT NULL COMMENT '找到时间'," +
                        "  `found_location_name` VARCHAR(100) DEFAULT NULL COMMENT '找到地点名称'," +
                        "  `health_status` TINYINT DEFAULT 0 COMMENT '健康状况 0:未知 1:健康 2:受伤 3:生病'," +
                        "  `behavior_description` TEXT DEFAULT NULL COMMENT '行为描述'," +
                        "  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间'," +
                        "  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间'," +
                        "  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除'," +
                        "  PRIMARY KEY (`id`)," +
                        "  KEY `idx_creator_id` (`creator_id`)," +
                        "  KEY `idx_pet_id` (`pet_id`)," +
                        "  KEY `idx_pet_type` (`pet_type`)," +
                        "  KEY `idx_is_stray` (`is_stray`)," +
                        "  KEY `idx_status` (`status`)," +
                        "  KEY `idx_lost_time` (`lost_time`)," +
                        "  KEY `idx_create_time` (`create_time`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='走失宠物表'";
                jdbcTemplate.execute(createTableSQL);
                log.info("✅ lost_pet 表创建成功");
            } else {
                log.info("✅ lost_pet 表已存在");
            }
        } catch (Exception e) {
            log.error("❌ 处理 lost_pet 表失败:", e);
        }
    }

    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return 如果表存在返回 true，否则返回 false
     */
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = (SELECT DATABASE()) " +
                    "AND TABLE_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{tableName}, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("检查表是否存在时出错，默认认为不存在: {}", e.getMessage());
            return false;
        }
    }
}

