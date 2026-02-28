-- 如果reminder表中没有remind_date字段，则添加该字段
SET @dbname = DATABASE();
SET @tablename = 'reminder';
SET @columnname = 'remind_date';

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DATE DEFAULT NULL COMMENT ''提醒日期（支持未来一年的日期）'' AFTER `title`;')
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

