-- 数据库迁移脚本：支持多用户管理同一宠物
-- 执行时间：2026年2月
-- 说明：此脚本将现有的单用户模型迁移到多用户共享模型

-- 第一步：添加创建者字段（如果pet表还有user_id，先改名）
-- ALTER TABLE `pet` CHANGE COLUMN `user_id` `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID';

-- 第二步：创建用户-宠物关联表
CREATE TABLE IF NOT EXISTS `user_pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `role` TINYINT DEFAULT 1 COMMENT '角色 1:拥有者 2:共享用户',
  `permission` VARCHAR(200) DEFAULT NULL COMMENT '权限(JSON)，如: [\"view\",\"edit\",\"manage\"]',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet` (`user_id`, `pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-宠物关联表';

-- 第三步：创建权限共享记录表
CREATE TABLE IF NOT EXISTS `pet_share_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `from_user_id` BIGINT NOT NULL COMMENT '分享者用户ID',
  `to_user_id` BIGINT NOT NULL COMMENT '被分享者用户ID',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型: share(分享),revoke(撤销),update(更新权限)',
  `permission` VARCHAR(200) DEFAULT NULL COMMENT '权限详情(JSON)',
  `create_time` DATETIME DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`from_user_id`, `to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物权限共享记录表';

-- 第四步：从现有的pet表数据迁移到user_pet表（只在第一次迁移时执行）
-- 如果pet表中的所有宠物都只属于一个用户（creator_id），则执行：
-- INSERT INTO `user_pet` (`user_id`, `pet_id`, `role`, `permission`, `create_time`, `update_time`, `deleted`)
-- SELECT `creator_id`, `id`, 1, '[\"view\",\"edit\",\"manage\"]', NOW(), NOW(), 0
-- FROM `pet` WHERE `deleted` = 0;

-- 第五步：为现有的关联表添加必要的约束（如果还没有）
-- 医疗记录表、疫苗记录表、领地表、遛狗记录表等已经有user_id和pet_id字段，无需改动

-- 第六步：验证数据一致性
-- SELECT COUNT(*) as pet_count FROM pet WHERE deleted = 0;
-- SELECT COUNT(*) as user_pet_count FROM user_pet WHERE deleted = 0;
-- 两个数字应该相等

-- 第七步：性能优化 - 添加必要的外键约束（可选）
-- ALTER TABLE `user_pet` ADD CONSTRAINT `fk_user_pet_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`);
-- ALTER TABLE `user_pet` ADD CONSTRAINT `fk_user_pet_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`);
-- ALTER TABLE `pet_share_log` ADD CONSTRAINT `fk_share_log_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`);

