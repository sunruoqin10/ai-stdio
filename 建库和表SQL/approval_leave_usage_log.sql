DROP TABLE IF EXISTS `approval_leave_usage_log`;
CREATE TABLE `approval_leave_usage_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `employee_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工ID',
  `request_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请ID',
  `type` enum('annual','sick','personal','comp_time','marriage','maternity') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请假类型',
  `duration` decimal(4,1) NOT NULL COMMENT '请假时长(天)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `change_type` enum('deduct','rollback') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变动类型:扣减/回退',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_usage_employee` (`employee_id`),
  KEY `idx_usage_request` (`request_id`),
  KEY `idx_usage_created` (`created_at`),
  KEY `idx_usage_type` (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年假使用记录表';
