# Phase 2 数据库改动说明

**完成日期**：2026年2月16日
**改动版本**：1.1
**状态**：Phase 2 后端实现完成

---

## 📋 改动概览

Phase 2 后端实现中对数据库的改动：

### 1. 表结构改动
- **pet 表**：添加 `creator_id` 字段（创建者用户ID）
- **user_pet 表**：已在 init.sql 中创建（Phase 1）
- **pet_share_log 表**：已在 init.sql 中创建（Phase 1）

### 2. 数据迁移策略

#### 选项 A：全新数据库（推荐）
```sql
-- 直接执行最新的 init.sql
mysql -u root -p your_db < backend/src/main/resources/sql/init.sql
```

#### 选项 B：现有数据库迁移

##### 步骤1：备份数据库
```bash
mysqldump -u root -p your_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

##### 步骤2：添加新字段到 pet 表
```sql
-- 添加 creator_id 字段
ALTER TABLE pet ADD COLUMN creator_id BIGINT NOT NULL DEFAULT 0 AFTER id COMMENT '创建者用户ID';

-- 为 creator_id 创建索引
CREATE INDEX idx_creator_id ON pet(creator_id);
```

##### 步骤3：迁移现有数据
```sql
-- 如果现有系统有 user_id 字段，将其迁移到 creator_id
UPDATE pet SET creator_id = user_id WHERE user_id IS NOT NULL AND deleted = 0;

-- 创建 user_pet 表（如果还未创建）
CREATE TABLE IF NOT EXISTS `user_pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `role` TINYINT DEFAULT 1 COMMENT '角色 1:拥有者 2:共享用户',
  `permission` VARCHAR(200) DEFAULT NULL COMMENT '权限(JSON)，如: ["view","edit","manage"]',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet` (`user_id`, `pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-宠物关联表';

-- 创建 pet_share_log 表（如果还未创建）
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
```

##### 步骤4：为所有现有宠物创建 user_pet 记录
```sql
-- 为每个宠物创建一条拥有者记录
INSERT INTO user_pet (user_id, pet_id, role, permission, create_time, update_time, deleted)
SELECT creator_id, id, 1, '["view","edit","manage"]', NOW(), NOW(), 0
FROM pet
WHERE deleted = 0
AND id NOT IN (SELECT pet_id FROM user_pet WHERE role = 1);
```

##### 步骤5：验证数据一致性
```sql
-- 检查是否有孤立的宠物（没有拥有者）
SELECT p.id, p.name FROM pet p
LEFT JOIN user_pet up ON p.id = up.pet_id AND up.role = 1
WHERE up.id IS NULL AND p.deleted = 0;

-- 检查宠物总数和 user_pet 拥有者记录数
SELECT
    'pet_count' AS metric, COUNT(*) AS count FROM pet WHERE deleted = 0
UNION ALL
SELECT
    'owner_records', COUNT(*) FROM user_pet WHERE role = 1 AND deleted = 0;

-- 检查 creator_id 是否正确赋值
SELECT COUNT(*) as pets_with_creator_id FROM pet WHERE creator_id > 0 AND deleted = 0;
```

##### 步骤6：可选 - 移除旧的 user_id 字段
```sql
-- 如果确认数据完全迁移，可以移除旧字段（需谨慎！）
-- ALTER TABLE pet DROP COLUMN user_id;
```

---

## 🔄 数据库兼容性

### 向后兼容性检查
- ✅ 旧的 `pet.user_id` 字段保留用于兼容（建议未来删除）
- ✅ 现有 API 继续使用 `getPetList(userId)` 工作
- ✅ 新的多用户 API 通过 `user_pet` 表实现

### 迁移后的行为

**旧 API**（`/pet/list/{userId}`）：
```
行为保持不变：查询用户拥有的宠物
内部：查询 user_pet 表中 role=1 的宠物
```

**新 API**（`/pet/my-pets/{userId}`）：
```
行为新增：查询用户拥有和共享的所有宠物
内部：查询 user_pet 表中所有关联的宠物
```

---

## 📊 SQL 改动汇总

### 新增的表
1. **user_pet** - 用户宠物关联表
2. **pet_share_log** - 权限分享记录表

### 修改的表
1. **pet** 表
   - 新增字段：`creator_id` (BIGINT)
   - 新增索引：`idx_creator_id`
   - 保留字段：`user_id`（为了兼容）

### 字段详解

#### user_pet 表字段
| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| id | BIGINT | 主键 | 1 |
| user_id | BIGINT | 用户ID | 100 |
| pet_id | BIGINT | 宠物ID | 5 |
| role | TINYINT | 角色：1=拥有者，2=共享用户 | 1 |
| permission | VARCHAR(200) | 权限JSON | ["view","edit","manage"] |
| create_time | DATETIME | 创建时间 | 2026-02-16 10:00:00 |
| update_time | DATETIME | 更新时间 | 2026-02-16 10:00:00 |
| deleted | TINYINT | 逻辑删除标记 | 0 |

#### pet_share_log 表字段
| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| id | BIGINT | 主键 | 1 |
| pet_id | BIGINT | 宠物ID | 5 |
| from_user_id | BIGINT | 分享者用户ID | 100 |
| to_user_id | BIGINT | 被分享者用户ID | 200 |
| action | VARCHAR(50) | 操作：share/revoke/update | "share" |
| permission | VARCHAR(200) | 权限JSON | ["view","edit"] |
| create_time | DATETIME | 操作时间 | 2026-02-16 10:00:00 |

---

## 🛡️ 数据验证脚本

### 完整的验证检查清单

```sql
-- 1. 检查新表是否存在
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'your_db'
AND TABLE_NAME IN ('user_pet', 'pet_share_log');
-- 应该返回 2 行

-- 2. 检查 creator_id 字段
DESC pet;
-- 应该有 creator_id 字段

-- 3. 检查数据一致性
SELECT COUNT(*) as total_pets FROM pet WHERE deleted = 0;
SELECT COUNT(*) as total_user_pet FROM user_pet WHERE deleted = 0 AND role = 1;
-- 这两个数字应该大约相等（可能相等）

-- 4. 检查是否有宠物没有拥有者
SELECT COUNT(*) as orphaned_pets
FROM pet p
LEFT JOIN user_pet up ON p.id = up.pet_id AND up.role = 1 AND up.deleted = 0
WHERE p.deleted = 0 AND up.id IS NULL;
-- 应该返回 0

-- 5. 检查是否有无效的 creator_id
SELECT COUNT(*) as invalid_creator_ids
FROM pet p
WHERE creator_id <= 0 AND deleted = 0;
-- 应该返回 0（或很少数量）

-- 6. 检查权限JSON格式
SELECT COUNT(*) as invalid_permissions
FROM user_pet
WHERE permission NOT LIKE '%["view"%' AND permission IS NOT NULL AND deleted = 0;
-- 应该返回 0 或较小数字

-- 7. 查看索引
SHOW INDEX FROM user_pet;
SHOW INDEX FROM pet_share_log;
-- 应该显示所有创建的索引
```

---

## ⚠️ 常见问题和解决方案

### Q1: 迁移后旧 API 还能用吗？
**A**：可以。但需要后端代码配合修改以支持通过 user_pet 表查询。旧代码会继续工作，但新增的宠物需要在 user_pet 表中创建记录。

### Q2: 如何处理 user_id 和 creator_id 的关系？
**A**：
- `pet.user_id` - 保留用于兼容旧系统，未来可删除
- `pet.creator_id` - 新系统中的宠物创建者
- 两者应该在迁移过程中保持同步

### Q3: 迁移过程中如何避免数据丢失？
**A**：
1. 执行迁移前必须备份数据库
2. 在测试数据库上先测试迁移脚本
3. 验证所有数据一致性检查都通过
4. 灰度发布，先发布到测试环境

### Q4: 如何回滚迁移？
**A**：
```bash
# 从备份恢复
mysql -u root -p your_db < backup_YYYYMMDD_HHMMSS.sql
```

### Q5: permission 字段中的 JSON 如何解析？
**A**：
- 存储格式：`["view","edit","manage"]`
- 在 Java 中使用 JSON 库（如 Gson、Jackson）解析
- 查询示例：`WHERE permission LIKE '%"edit"%'`

---

## 🔐 性能影响分析

### 索引策略
```sql
-- user_pet 表的索引已优化
-- 主查询场景：
-- 1. 查询用户的所有宠物 → 使用 idx_user_id
-- 2. 查询宠物的所有用户 → 使用 idx_pet_id
-- 3. 查询特定用户-宠物关系 → 使用 UNIQUE KEY (user_id, pet_id)

-- pet_share_log 表的索引已优化
-- 主查询场景：
-- 1. 查询宠物的分享历史 → 使用 idx_pet_id
-- 2. 查询用户的分享操作 → 使用 idx_user_id
```

### 性能预期
- ✅ 单个用户查询宠物：O(log n)，索引查询
- ✅ 查询所有共享用户：O(log n)，索引查询
- ✅ 分享操作：O(1)，直接插入
- ✅ 权限检查：O(log n)，索引查询

### 性能监控
```sql
-- 监控表大小
SELECT
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'your_db'
AND table_name IN ('pet', 'user_pet', 'pet_share_log');

-- 监控查询性能
EXPLAIN SELECT * FROM user_pet WHERE user_id = 100;
EXPLAIN SELECT * FROM pet_share_log WHERE pet_id = 5;
```

---

## 📝 迁移清单

### 迁移前检查
- [ ] 备份现有数据库
- [ ] 确认测试环境可用
- [ ] 准备回滚方案
- [ ] 通知相关团队

### 迁移执行
- [ ] 在测试环境执行迁移脚本
- [ ] 验证数据一致性
- [ ] 运行完整的单元测试
- [ ] 测试所有 API 端点

### 迁移验证
- [ ] 执行所有数据验证 SQL
- [ ] 检查索引创建情况
- [ ] 性能基准测试
- [ ] 灰度发布第一批用户

### 迁移后维护
- [ ] 监控数据库性能
- [ ] 收集用户反馈
- [ ] 监测权限检查是否正常
- [ ] 定期备份数据库

---

## 🔗 相关文档

- **设计文档**：`MULTI_USER_DESIGN.md`
- **快速开始**：`QUICK_START_MULTI_USER.md`
- **实现检查单**：`IMPLEMENTATION_CHECKLIST.md`
- **迁移脚本**：`migrate_multi_user_support.sql`

---

## 📞 技术支持

如有问题，参考以下文档：

1. 数据一致性问题 → 运行验证 SQL 脚本
2. 性能问题 → 检查索引和执行计划
3. 应用错误 → 查看日志中的 SQL 错误
4. 迁移失败 → 从备份恢复并检查错误

---

**改动完成时间**：2026年2月16日
**数据库版本**：1.1
**状态**：Phase 2 - 后端实现完成 ✅

