SET @active_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'cliente'
    AND COLUMN_NAME = 'activo'
);
SET @active_column_migration = IF(
  @active_column_exists = 0,
  'ALTER TABLE cliente ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT ''cliente.activo already exists'''
);
PREPARE active_column_statement FROM @active_column_migration;
EXECUTE active_column_statement;
DEALLOCATE PREPARE active_column_statement;
