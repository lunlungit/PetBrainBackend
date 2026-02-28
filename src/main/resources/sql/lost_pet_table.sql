-- ============================================
-- 走失宠物模块 SQL 脚本
-- ============================================
-- 说明：创建新的 lost_pet 表，用于统一管理所有走失宠物信息
-- 区分：
--   1. 用户发布的走失宠物（is_stray=0，pet_type=1/2/3）
--   2. 用户发现的流浪宠物（is_stray=1，pet_type=1/2/3）

-- ============================================
-- 1. 创建走失宠物表
-- ============================================
CREATE TABLE IF NOT EXISTS `lost_pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT DEFAULT NULL COMMENT '关联的宠物ID（如果是用户自己的宠物，否则为NULL）',
  `creator_id` BIGINT NOT NULL COMMENT '发布者/上报者用户ID',
  `pet_type` TINYINT NOT NULL COMMENT '宠物类型 1:狗 2:猫 3:其他',
  `is_stray` TINYINT DEFAULT 0 COMMENT '是否流浪宠物 0:用户发布的走失宠物 1:用户发现的流浪宠物',
  `pet_name` VARCHAR(50) DEFAULT NULL COMMENT '宠物名称',
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种描述',
  `color` VARCHAR(100) DEFAULT NULL COMMENT '毛色/外观特征',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '宠物照片URL',
  `description` TEXT COMMENT '详细描述',

  -- 走失/发现信息
  `lost_time` DATETIME DEFAULT NULL COMMENT '走失/发现时间',
  `lost_location_name` VARCHAR(100) DEFAULT NULL COMMENT '走失/发现地点名称',
  `lost_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '走失/发现地点纬度',
  `lost_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '走失/发现地点经度',

  -- 联系信息
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人名称',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `contact_wechat` VARCHAR(50) DEFAULT NULL COMMENT '联系人微信号',

  -- 状态信息
  `status` TINYINT DEFAULT 0 COMMENT '状态 0:走失中/流浪中 1:已找到',
  `found_time` DATETIME DEFAULT NULL COMMENT '找到时间',
  `found_location_name` VARCHAR(100) DEFAULT NULL COMMENT '找到地点名称',

  -- 流浪宠物特定字段
  `health_status` TINYINT DEFAULT 0 COMMENT '健康状况 0:未知 1:健康 2:受伤 3:生病',
  `behavior_description` TEXT DEFAULT NULL COMMENT '行为描述（友好度、反应等）',

  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_pet_type` (`pet_type`),
  KEY `idx_is_stray` (`is_stray`),
  KEY `idx_status` (`status`),
  KEY `idx_lost_time` (`lost_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='走失宠物表（统一管理用户发布的走失宠物和发现的流浪宠物）';

-- ============================================
-- 2. 从 pet 表迁移走失宠物数据到 lost_pet 表
-- ============================================
-- 说明：如果已有走失宠物数据存储在 pet 表中，执行以下 SQL 迁移数据

-- 将 pet 表中 status=2（走失中）的宠物迁移到 lost_pet 表
-- INSERT INTO `lost_pet` (pet_id, creator_id, pet_type, pet_name, breed, color, avatar, description, lost_latitude, lost_longitude, contact_name, contact_phone, status, lost_time, is_stray, create_time, update_time, deleted)
-- SELECT id, creator_id, type, name, breed, color, avatar, NULL, NULL, NULL, NULL, NULL, 0, NOW(), 0, create_time, update_time, deleted
-- FROM `pet`
-- WHERE status = 2 AND deleted = 0;

-- ============================================
-- 3. 可选：从 pet 表中删除走失相关字段
-- ============================================
-- 说明：如果要完全分离，可以删除 pet 表中的走失相关字段
-- 但建议保留以维持向后兼容性

-- ALTER TABLE `pet` DROP COLUMN IF EXISTS `lost_latitude`;
-- ALTER TABLE `pet` DROP COLUMN IF EXISTS `lost_longitude`;
-- ALTER TABLE `pet` DROP COLUMN IF EXISTS `lost_time`;

-- ============================================
-- 4. 字段说明
-- ============================================
/*
pet_id:
  - 如果是用户自己的宠物发生走失，则引用 pet 表的 ID
  - 如果是发现的流浪宠物，则为 NULL

creator_id: 发布或上报者的用户 ID

pet_type: 宠物类型（所有类型都可以是走失或流浪的）
  - 1: 狗
  - 2: 猫
  - 3: 其他

is_stray: 是否流浪宠物
  - 0: 用户发布的走失宠物（pet_id 一般不为 NULL）
  - 1: 用户发现的流浪宠物（pet_id 一般为 NULL）

status: 状态（只有两种）
  - 0: 走失中/流浪中（未找到）
  - 1: 已找到（已救助/已寻回）

health_status: 健康状况（主要用于流浪宠物）
  - 0: 未知
  - 1: 健康
  - 2: 受伤
  - 3: 生病

location_name 和坐标: 用于显示走失/发现地点和查询附近走失宠物

behavior_description: 行为描述（主要用于流浪宠物，描述友好度、是否咬人等）
*/

-- ============================================
-- 5. 区分用户发布的走失宠物和流浪宠物
-- ============================================
-- 用户发布的走失宠物：is_stray = 0
-- 用户发现的流浪宠物：is_stray = 1

-- ============================================
-- 6. 查询示例
-- ============================================
-- 查询附近走失中的宠物（用户发布的走失宠物）
-- SELECT * FROM `lost_pet`
-- WHERE status = 0 AND is_stray = 0 AND deleted = 0
-- AND SQRT(POW(RADIANS(lost_latitude - ?), 2) + POW(RADIANS(lost_longitude - ?), 2) * POW(COS(RADIANS(lost_latitude)), 2)) * 6371 <= 5
-- ORDER BY lost_time DESC;

-- 查询附近流浪中的宠物（用户发现的流浪宠物）
-- SELECT * FROM `lost_pet`
-- WHERE status = 0 AND is_stray = 1 AND deleted = 0
-- AND SQRT(POW(RADIANS(lost_latitude - ?), 2) + POW(RADIANS(lost_longitude - ?), 2) * POW(COS(RADIANS(lost_latitude)), 2)) * 6371 <= 5
-- ORDER BY create_time DESC;

-- 查询已找到的宠物
-- SELECT * FROM `lost_pet`
-- WHERE status = 1 AND deleted = 0
-- ORDER BY found_time DESC;
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种描述',
  `color` VARCHAR(100) DEFAULT NULL COMMENT '毛色/外观特征',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '宠物照片URL',
  `description` TEXT COMMENT '详细描述',

  -- 走失/发现信息
  `lost_time` DATETIME DEFAULT NULL COMMENT '走失/发现时间',
  `lost_location_name` VARCHAR(100) DEFAULT NULL COMMENT '走失/发现地点名称',
  `lost_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '走失/发现地点纬度',
  `lost_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '走失/发现地点经度',

  -- 联系信息
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人名称',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `contact_wechat` VARCHAR(50) DEFAULT NULL COMMENT '联系人微信号',

  -- 状态信息
  `status` TINYINT DEFAULT 0 COMMENT '状态 0:走失中/流浪中 1:已找到',
  `found_time` DATETIME DEFAULT NULL COMMENT '找到时间',
  `found_location_name` VARCHAR(100) DEFAULT NULL COMMENT '找到地点名称',

  -- 流浪宠物特定字段
  `health_status` TINYINT DEFAULT 0 COMMENT '健康状况 0:未知 1:健康 2:受伤 3:生病',
  `behavior_description` TEXT DEFAULT NULL COMMENT '行为描述（友好度、反应等）',

  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_pet_type` (`pet_type`),
  KEY `idx_status` (`status`),
  KEY `idx_lost_time` (`lost_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='走失宠物表（统一管理用户发布的走失宠物和发现的流浪宠物）';

-- ============================================
-- 2. 从 pet 表迁移走失宠物数据到 lost_pet 表
-- ============================================
-- 说明：如果已有走失宠物数据存储在 pet 表中，执行以下 SQL 迁移数据

-- 将 pet 表中 status=2（走失中）的宠物迁移到 lost_pet 表
-- INSERT INTO `lost_pet` (pet_id, creator_id, pet_type, pet_name, breed, color, avatar, description, lost_latitude, lost_longitude, contact_name, contact_phone, status, lost_time, create_time, update_time, deleted)
-- SELECT id, creator_id, type, name, breed, color, avatar, NULL, NULL, NULL, NULL, NULL, 0, NOW(), create_time, update_time, deleted
-- FROM `pet`
-- WHERE status = 2 AND deleted = 0;

-- ============================================
-- 3. 可选：从 pet 表中删除走失相关字段
-- ============================================
-- 说明：如果要完全分离，可以删除 pet 表中的走失相关字段
-- 但建议保留以维持向后兼容性

-- ALTER TABLE `pet` DROP COLUMN IF EXISTS `lost_latitude`;
-- ALTER TABLE `pet` DROP COLUMN IF EXISTS `lost_longitude`;
-- ALTER TABLE `pet` DROP COLUMN IF EXISTS `lost_time`;

-- ============================================
-- 4. 字段说明
-- ============================================
/*
pet_id:
  - 如果是用户自己的宠物发生走失，则引用 pet 表的 ID
  - 如果是发现的流浪宠物，则为 NULL

creator_id: 发布或上报者的用户 ID

pet_type: 宠物类型（区分走失和流浪）
  - 1: 狗（用户发布的走失宠物）
  - 2: 猫（用户发布的走失宠物）
  - 3: 流浪狗（用户发现上报）
  - 4: 流浪猫（用户发现上报）
  - 5: 其他

status: 状态（只有两种）
  - 0: 走失中/流浪中（未找到）
  - 1: 已找到（已救助/已寻回）

health_status: 健康状况（主要用于流浪宠物）
  - 0: 未知
  - 1: 健康
  - 2: 受伤
  - 3: 生病

location_name 和坐标: 用于显示走失/发现地点和查询附近走失宠物

behavior_description: 行为描述（主要用于流浪宠物，描述友好度、是否咬人等）
*/

-- ============================================
-- 5. 区分用户发布的走失宠物和流浪宠物
-- ============================================
-- 用户发布的走失宠物：pet_type IN (1, 2) 且 pet_id 不为 NULL
-- 发现的流浪宠物：pet_type IN (3, 4) 且 pet_id 为 NULL

-- ============================================
-- 6. 查询示例
-- ============================================
-- 查询附近走失中的宠物（用户发布）
-- SELECT * FROM `lost_pet`
-- WHERE status = 0 AND pet_type IN (1, 2) AND deleted = 0
-- AND SQRT(POW(RADIANS(lost_latitude - ?), 2) + POW(RADIANS(lost_longitude - ?), 2) * POW(COS(RADIANS(lost_latitude)), 2)) * 6371 <= 5
-- ORDER BY lost_time DESC;

-- 查询附近流浪中的宠物（用户发现）
-- SELECT * FROM `lost_pet`
-- WHERE status = 0 AND pet_type IN (3, 4) AND deleted = 0
-- AND SQRT(POW(RADIANS(lost_latitude - ?), 2) + POW(RADIANS(lost_longitude - ?), 2) * POW(COS(RADIANS(lost_latitude)), 2)) * 6371 <= 5
-- ORDER BY create_time DESC;

-- 查询已找到的宠物
-- SELECT * FROM `lost_pet`
-- WHERE status = 1 AND deleted = 0
-- ORDER BY found_time DESC;

