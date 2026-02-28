-- 用户表 (数据库: grocery_configuration)
CREATE TABLE IF NOT EXISTS `user_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 宠物表（去掉user_id，宠物与用户通过user_pet表关联）
CREATE TABLE IF NOT EXISTS `pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `type` TINYINT NOT NULL COMMENT '类型 1:狗 2:猫 3:其他',
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0:公 1:母',
  `sterilized` TINYINT DEFAULT 0 COMMENT '是否绝育 0:未绝育 1:已绝育',
  `color` VARCHAR(50) DEFAULT NULL COMMENT '毛色',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0:正常 1:生病 2:走失',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- 用户-宠物关联表（支持多用户管理同一宠物）
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

-- 医疗记录表
CREATE TABLE IF NOT EXISTS `medical_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `hospital` VARCHAR(100) DEFAULT NULL COMMENT '医院名称',
  `doctor` VARCHAR(50) DEFAULT NULL COMMENT '医生姓名',
  `diagnosis` TEXT COMMENT '诊断结果',
  `symptoms` TEXT COMMENT '症状描述',
  `prescription` TEXT COMMENT '处方药',
  `cost` DECIMAL(10,2) DEFAULT NULL COMMENT '费用',
  `visit_date` DATETIME DEFAULT NULL COMMENT '就诊时间',
  `images` TEXT COMMENT '附件照片(JSON数组)',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗记录表';

-- 疫苗记录表
CREATE TABLE IF NOT EXISTS `vaccination` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `vaccine_name` VARCHAR(100) NOT NULL COMMENT '疫苗名称',
  `vaccine_date` DATE DEFAULT NULL COMMENT '接种日期',
  `next_vaccine_date` DATE DEFAULT NULL COMMENT '下次接种日期',
  `hospital` VARCHAR(100) DEFAULT NULL COMMENT '接种医院',
  `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗记录表';

-- 领地标记表
CREATE TABLE IF NOT EXISTS `territory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `location_name` VARCHAR(100) DEFAULT NULL COMMENT '地点名称',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `mark_count` INT DEFAULT 1 COMMENT '标记次数',
  `last_mark_time` DATETIME DEFAULT NULL COMMENT '最后标记时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_pet` (`user_id`, `pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领地标记表';

-- 遛狗记录表
CREATE TABLE IF NOT EXISTS `walk_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `walk_date` DATE NOT NULL COMMENT '遛狗日期',
  `steps` INT DEFAULT 0 COMMENT '步数',
  `distance` DECIMAL(10,2) DEFAULT NULL COMMENT '距离(km)',
  `duration` INT DEFAULT NULL COMMENT '时长(分钟)',
  `mark_count` INT DEFAULT 0 COMMENT '标记次数',
  `route_path` TEXT COMMENT '路径点(JSON)',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_walk_date` (`walk_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='遛狗记录表';

-- 勋章成就表
CREATE TABLE IF NOT EXISTS `achievement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID',
  `type` VARCHAR(50) NOT NULL COMMENT '勋章类型',
  `name` VARCHAR(100) NOT NULL COMMENT '勋章名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '勋章描述',
  `icon` VARCHAR(500) DEFAULT NULL COMMENT '勋章图标URL',
  `unlock_time` DATETIME DEFAULT NULL COMMENT '解锁时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勋章成就表';

-- 账本支出表
CREATE TABLE IF NOT EXISTS `expense` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `category` TINYINT NOT NULL COMMENT '分类 1:食品 2:医疗 3:玩具 4:洗护 5:其他',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `expense_date` DATE NOT NULL COMMENT '支出日期',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '凭证图片',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_expense_date` (`expense_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本支出表';

-- 食物查询表
CREATE TABLE IF NOT EXISTS `food_query` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `food_name` VARCHAR(100) NOT NULL COMMENT '食物名称',
  `result` TINYINT NOT NULL COMMENT '结果 0:可以吃 1:少吃 2:不能吃',
  `description` TEXT COMMENT '描述说明',
  `nutrition` TEXT COMMENT '营养价值',
  `pet_type` TINYINT NOT NULL COMMENT '宠物类型 1:狗 2:猫',
  `create_time` VARCHAR(50) DEFAULT NULL COMMENT '创建时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_food_name` (`food_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物查询表';

-- 提醒表
CREATE TABLE IF NOT EXISTS `reminder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `type` TINYINT NOT NULL COMMENT '提醒类型 1:喂食 2:遛狗 3:驱虫 4:洗澡 5:打疫苗 6:复查',
  `title` VARCHAR(100) NOT NULL COMMENT '提醒标题',
  `remind_date` DATE DEFAULT NULL COMMENT '提醒日期（支持未来一年的日期）',
  `remind_time` TIME DEFAULT NULL COMMENT '提醒时间',
  `repeat_type` TINYINT DEFAULT 0 COMMENT '重复类型 0:一次 1:每天 2:每周 3:每月',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0:待完成 1:已完成',
  `last_trigger_time` DATETIME DEFAULT NULL COMMENT '最后触发时间',
  `last_push_time` DATETIME DEFAULT NULL COMMENT '上次推送时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_pet` (`user_id`, `pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒表';

-- 插入示例食物查询数据
INSERT INTO `food_query` (`food_name`, `result`, `description`, `nutrition`, `pet_type`) VALUES
('苹果', 0, '可以适量食用，去核去籽', '富含维生素C和膳食纤维，有助消化', 1),
('香蕉', 0, '可以适量食用', '富含钾和维生素B6，能量补充佳品', 1),
('巧克力', 2, '绝对不能吃！含有可可碱，对狗有毒', '有毒，严重可致死亡', 1),
('葡萄', 2, '绝对不能吃！可能导致肾衰竭', '有毒，绝对禁止', 1),
('洋葱', 2, '绝对不能吃！可能导致贫血', '有毒，绝对禁止', 1),
('鸡胸肉', 0, '可以吃，要煮熟去骨', '优质蛋白质来源', 1),
('胡萝卜', 0, '可以适量食用', '富含维生素A和胡萝卜素', 1),
('蓝莓', 0, '可以适量食用', '富含抗氧化剂', 1),
('三文鱼', 0, '可以吃，要煮熟', '富含Omega-3脂肪酸，对皮肤毛发有益', 1),
('奶酪', 1, '少量食用，注意乳糖不耐受', '高钙高蛋白', 1),
('牛奶', 1, '部分狗狗乳糖不耐受，建议少量', '高钙', 1),
('鸡蛋', 0, '可以吃，要煮熟', '优质蛋白质', 1),
('米饭', 0, '可以适量食用', '碳水化合物来源', 1),
('南瓜', 0, '可以吃', '富含纤维，有助消化', 1),
('红薯', 0, '可以吃', '富含纤维和维生素', 1);

-- 猫咪食物数据
INSERT INTO `food_query` (`food_name`, `result`, `description`, `nutrition`, `pet_type`) VALUES
('鱼', 0, '可以吃，要煮熟去刺', '优质蛋白质，富含牛磺酸', 2),
('鸡肉', 0, '可以吃，要煮熟', '优质蛋白质', 2),
('鸡蛋', 0, '可以吃，要煮熟', '优质蛋白质', 2),
('牛奶', 1, '大部分猫咪乳糖不耐受，建议少量', '高钙', 2),
('巧克力', 2, '绝对不能吃！含有可可碱，对猫有毒', '有毒，严重可致死亡', 2),
('洋葱', 2, '绝对不能吃！可能导致贫血', '有毒，绝对禁止', 2),
('大蒜', 2, '绝对不能吃！可能导致贫血', '有毒，绝对禁止', 2),
('葡萄', 2, '绝对不能吃！可能导致肾衰竭', '有毒，绝对禁止', 2),
('蓝莓', 0, '可以少量食用', '富含抗氧化剂', 2),
('南瓜', 0, '可以吃', '富含纤维，有助消化', 2);

-- 体重记录表
CREATE TABLE IF NOT EXISTS `weight_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
  `record_date` DATE NOT NULL COMMENT '记录日期(yyyy-MM-dd)',
  `record_month` TINYINT NOT NULL COMMENT '记录月份(1-12)',
  `record_year` INT NOT NULL COMMENT '记录年份(YYYY)',
  `note` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_date` (`pet_id`, `record_date`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物体重记录表';

-- 权限共享记录表（记录宠物权限共享历史）
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

-- 走失宠物表（统一管理所有走失宠物信息：包括用户发布的走失宠物和发现的流浪宠物）
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

-- 宠物分享码表
CREATE TABLE IF NOT EXISTS `pet_share_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pet_id` bigint NOT NULL COMMENT '宠物ID',
  `user_id` bigint NOT NULL COMMENT '生成分享码的用户ID（宠物所有者）',
  `share_code` varchar(100) NOT NULL UNIQUE COMMENT '分享码',
  `share_url` varchar(255) NOT NULL COMMENT '分享链接',
  `permission` varchar(20) COMMENT '权限级别 READ:查看 WRITE:编辑',
  `expire_time` datetime COMMENT '过期时间（null表示永不过期）',
  `remaining_uses` int COMMENT '剩余使用次数（null表示无限制）',
  `used_count` int DEFAULT 0 COMMENT '已被使用的次数',
  `active` tinyint DEFAULT 1 COMMENT '是否激活',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_code` (`share_code`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物分享码表';

