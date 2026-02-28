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

