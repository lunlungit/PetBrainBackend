-- 为 lost_pet 表添加多张照片支持字段
ALTER TABLE lost_pet ADD COLUMN IF NOT EXISTS images LONGTEXT COMMENT '宠物多张照片URL（JSON数组格式）';

-- 添加索引以提高查询性能
ALTER TABLE lost_pet ADD INDEX IF NOT EXISTS idx_lost_pet_creator_id (creator_id);
ALTER TABLE lost_pet ADD INDEX IF NOT EXISTS idx_lost_pet_status (status);
ALTER TABLE lost_pet ADD INDEX IF NOT EXISTS idx_lost_pet_is_stray (is_stray);

