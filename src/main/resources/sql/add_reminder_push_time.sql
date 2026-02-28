-- 为已存在的reminder表添加last_push_time字段
-- 如果表是新创建的，这个字段已经在init.sql中定义了
-- 执行前请先检查表中是否已存在该字段

-- 检查并添加字段（MySQL语法）
-- ALTER TABLE `reminder` ADD COLUMN IF NOT EXISTS `last_push_time` DATETIME DEFAULT NULL COMMENT '上次推送时间' AFTER `last_trigger_time`;

-- 如果MySQL版本不支持IF NOT EXISTS，可以使用以下SQL检查
ALTER TABLE `reminder` ADD COLUMN `last_push_time` DATETIME DEFAULT NULL COMMENT '上次推送时间' AFTER `last_trigger_time`;

