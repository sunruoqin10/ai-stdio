DROP TABLE IF EXISTS `approval_leave_balance`;
CREATE TABLE `approval_leave_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工ID',
  `year` int NOT NULL COMMENT '年份',
  `annual_total` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '年假总额(天)',
  `annual_used` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '已使用(天)',
  `annual_remaining` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '剩余(天)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_id` (`employee_id`),
  UNIQUE KEY `uk_leave_employee_year` (`employee_id`,`year`),
  KEY `idx_leave_balance_year` (`year`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年假余额表';
