-- 删除 sys_employee 表中的 probation_status 字段
-- 该字段已改为动态计算，不再需要存储

ALTER TABLE sys_employee DROP COLUMN probation_status;
