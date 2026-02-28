-- 为已存在的reminder表添加remind_date字段
ALTER TABLE `reminder` ADD COLUMN `remind_date` DATE DEFAULT NULL COMMENT '提醒日期（支持未来一年的日期）' AFTER `title`;

