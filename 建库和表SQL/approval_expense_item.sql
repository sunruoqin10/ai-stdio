DROP TABLE IF EXISTS `approval_expense_item`;
CREATE TABLE `approval_expense_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `expense_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报销单号',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '费用说明',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `expense_date` date NOT NULL COMMENT '发生日期',
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '费用分类',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_expense_item_expense` (`expense_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用明细表';
