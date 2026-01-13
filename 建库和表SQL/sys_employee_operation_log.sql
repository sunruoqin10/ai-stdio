DROP TABLE IF EXISTS `sys_employee_operation_log`;
CREATE TABLE `sys_employee_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `employee_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工ID',
  `operation` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operator` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人姓名',
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `details` text COLLATE utf8mb4_unicode_ci COMMENT '详细信息',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户代理',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工操作日志表';
