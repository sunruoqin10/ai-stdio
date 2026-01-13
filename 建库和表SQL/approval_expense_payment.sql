DROP TABLE IF EXISTS `approval_expense_payment`;
CREATE TABLE `approval_expense_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `expense_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报销单号',
  `amount` decimal(10,2) NOT NULL COMMENT '打款金额',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'bank_transfer' COMMENT '打款方式: bank_transfer/cash/check',
  `payment_date` date NOT NULL COMMENT '打款日期',
  `bank_account` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收款账号',
  `proof` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '打款凭证URL',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '状态: pending/completed/failed',
  `remark` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_expense` (`expense_id`),
  KEY `idx_payment_status` (`status`),
  KEY `idx_payment_date` (`payment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打款记录表';
