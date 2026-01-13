DROP TABLE IF EXISTS `approval_expense_invoice`;
CREATE TABLE `approval_expense_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `expense_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报销单号',
  `invoice_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发票类型',
  `invoice_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发票号码',
  `amount` decimal(10,2) NOT NULL COMMENT '发票金额',
  `invoice_date` date NOT NULL COMMENT '开票日期',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发票图片URL',
  `verified` tinyint(1) DEFAULT '0' COMMENT '是否已验真',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_number` (`invoice_number`),
  KEY `idx_invoice_expense` (`expense_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票表';
