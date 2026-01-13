DROP TABLE IF EXISTS `approval_leave_approval`;
CREATE TABLE `approval_leave_approval` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请ID',
  `approver_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审批人姓名',
  `approval_level` int NOT NULL COMMENT '审批层级',
  `status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '审批状态',
  `opinion` text COLLATE utf8mb4_unicode_ci COMMENT '审批意见',
  `timestamp` datetime DEFAULT NULL COMMENT '审批时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leave_request_approver` (`request_id`,`approver_id`),
  KEY `idx_leave_approver` (`approver_id`),
  KEY `idx_leave_approval_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批记录表';
