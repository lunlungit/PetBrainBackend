-- ================================================================================
-- Phase 2 后端实现 - 数据库必要改动 SQL 脚本
-- 更新日期：2026年2月16日
-- 说明：包含所有必要的数据库改动，用于支持 Phase 2 的多用户功能实现
-- ================================================================================

-- ================================================================================
-- 第1步：在 pet 表添加 creator_id 字段
-- ================================================================================

-- 如果 creator_id 字段不存在，添加它
ALTER TABLE `pet` ADD COLUMN `creator_id` BIGINT COMMENT '创建者用户ID' AFTER `id`;

-- 为 creator_id 创建索引（如果不存在）
-- 检查索引是否存在：SHOW INDEX FROM pet WHERE Column_name = 'creator_id';
CREATE INDEX IF NOT EXISTS `idx_creator_id` ON `pet` (`creator_id`);

-- ================================================================================
-- 第2步：迁移现有数据 - 将 user_id 复制到 creator_id
-- ================================================================================

-- 对于现有的宠物，将 user_id 设置为 creator_id
UPDATE `pet`
SET `creator_id` = `user_id`
WHERE `user_id` IS NOT NULL
AND `user_id` > 0
AND `deleted` = 0
AND (`creator_id` IS NULL OR `creator_id` = 0);

-- ================================================================================
-- 第3步：为所有现有宠物创建 user_pet 拥有者记录
-- ================================================================================

-- 插入所有现有宠物作为拥有者
INSERT INTO `user_pet` (`user_id`, `pet_id`, `role`, `permission`, `create_time`, `update_time`, `deleted`)
SELECT
    `creator_id`,
    `id`,
    1,  -- role: 1 = 拥有者
    '["view","edit","manage"]',  -- 默认权限：查看、编辑、管理
    NOW(),
    NOW(),
    0
FROM `pet`
WHERE `deleted` = 0
AND `id` NOT IN (
    SELECT `pet_id` FROM `user_pet`
    WHERE `role` = 1 AND `deleted` = 0
)
AND `creator_id` IS NOT NULL
AND `creator_id` > 0;

-- ================================================================================
-- 第4步：数据验证和一致性检查
-- ================================================================================

-- 检查1：验证 creator_id 是否正确赋值
-- 预期：返回值应该接近总宠物数
SELECT COUNT(*) AS pets_with_creator_id FROM `pet` WHERE `creator_id` > 0 AND `deleted` = 0;

-- 检查2：验证 user_pet 表中是否有所有宠物的拥有者记录
-- 预期：返回值应该为 0（没有孤立的宠物）
SELECT COUNT(*) AS orphaned_pets
FROM `pet` p
LEFT JOIN `user_pet` up ON p.`id` = up.`pet_id` AND up.`role` = 1 AND up.`deleted` = 0
WHERE p.`deleted` = 0 AND up.`id` IS NULL;

-- 检查3：对比宠物总数和拥有者记录数
-- 预期：两个数字应该大约相等
SELECT
    (SELECT COUNT(*) FROM `pet` WHERE `deleted` = 0) AS total_pets,
    (SELECT COUNT(*) FROM `user_pet` WHERE `role` = 1 AND `deleted` = 0) AS owner_records;

-- 检查4：检查权限 JSON 格式
-- 预期：返回值应该为 0
SELECT COUNT(*) AS invalid_permissions
FROM `user_pet`
WHERE `permission` NOT LIKE '%["view"%'
AND `permission` IS NOT NULL
AND `deleted` = 0;

-- ================================================================================
-- 第5步：索引验证
-- ================================================================================

-- 查看 user_pet 表的所有索引
-- SHOW INDEX FROM `user_pet`;

-- 查看 pet_share_log 表的所有索引
-- SHOW INDEX FROM `pet_share_log`;

-- ================================================================================
-- 【可选步骤】数据回滚
-- ================================================================================

-- 如果需要回滚，可以执行以下操作：

-- 回滚1：删除所有由迁移脚本插入的 user_pet 记录（保留原有记录）
-- DELETE FROM `user_pet`
-- WHERE `created_by_migration` = 1;  -- 需要有标记字段

-- 回滚2：清空 creator_id 字段
-- UPDATE `pet` SET `creator_id` = 0;

-- 回滚3：删除新增的索引
-- DROP INDEX `idx_creator_id` ON `pet`;

-- ================================================================================
-- 【调试 SQL】实时查询
-- ================================================================================

-- 查询用户拥有的所有宠物
-- SELECT p.* FROM `pet` p
-- JOIN `user_pet` up ON p.`id` = up.`pet_id`
-- WHERE up.`user_id` = 100 AND up.`role` = 1 AND up.`deleted` = 0 AND p.`deleted` = 0;

-- 查询某个宠物的所有共享用户
-- SELECT u.*, up.`role`, up.`permission` FROM `user_pet` up
-- JOIN `user_info` u ON up.`user_id` = u.`id`
-- WHERE up.`pet_id` = 5 AND up.`deleted` = 0;

-- 查询某个宠物的分享历史
-- SELECT * FROM `pet_share_log` WHERE `pet_id` = 5 ORDER BY `create_time` DESC;

-- ================================================================================
-- 【备份和恢复】
-- ================================================================================

-- 备份命令（在终端执行）
-- mysqldump -u root -p your_db > backup_$(date +%Y%m%d_%H%M%S).sql

-- 恢复命令（在终端执行）
-- mysql -u root -p your_db < backup_YYYYMMDD_HHMMSS.sql

-- ================================================================================
-- 【性能监控】
-- ================================================================================

-- 查看表大小
-- SELECT
--     table_name,
--     ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
-- FROM information_schema.TABLES
-- WHERE table_schema = 'your_db'
-- AND table_name IN ('pet', 'user_pet', 'pet_share_log');

-- 查询执行计划
-- EXPLAIN SELECT * FROM `user_pet` WHERE `user_id` = 100;
-- EXPLAIN SELECT * FROM `pet_share_log` WHERE `pet_id` = 5;

-- ================================================================================
-- 【说明】
-- ================================================================================
--
-- 本脚本包含 Phase 2 后端实现的所有数据库改动
--
-- 执行前注意：
-- 1. 务必备份现有数据库
-- 2. 在测试环境先验证脚本
-- 3. 确保所有表都已由 init.sql 创建
-- 4. 检查所有验证 SQL 都返回预期结果
--
-- 改动内容：
-- 1. 添加 creator_id 字段到 pet 表
-- 2. 迁移现有 user_id 数据到 creator_id
-- 3. 为所有现有宠物创建 user_pet 拥有者记录
-- 4. 验证数据一致性
--
-- 执行时间：约 1-5 分钟（取决于数据量）
--
-- 相关文档：
-- - PHASE2_DATABASE_CHANGES.md（详细说明）
-- - MULTI_USER_DESIGN.md（架构设计）
-- - QUICK_START_MULTI_USER.md（快速开始）
--
-- ================================================================================

