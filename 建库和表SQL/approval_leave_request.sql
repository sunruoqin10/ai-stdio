DROP TABLE IF EXISTS `approval_leave_request`;
CREATE TABLE `approval_leave_request` (
  `id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编号: LEAVE+YYYYMMDD+序号',
  `applicant_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请人ID',
  `department_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门ID',
  `type` enum('annual','sick','personal','comp_time','marriage','maternity') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请假类型',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `duration` decimal(4,1) NOT NULL COMMENT '请假时长(天)',
  `reason` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请假事由',
  `attachments` json DEFAULT NULL COMMENT '附件URL数组',
  `status` enum('draft','pending','approving','approved','rejected','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft' COMMENT '状态',
  `current_approval_level` int DEFAULT '0' COMMENT '当前审批层级',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_leave_applicant` (`applicant_id`),
  KEY `idx_leave_department` (`department_id`),
  KEY `idx_leave_status` (`status`),
  KEY `idx_leave_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';
