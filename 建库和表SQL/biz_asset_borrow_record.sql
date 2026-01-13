DROP TABLE IF EXISTS `biz_asset_borrow_record`;
CREATE TABLE `biz_asset_borrow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `asset_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资产ID',
  `asset_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资产名称',
  `borrower_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '借用人ID',
  `borrower_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '借用人姓名',
  `borrow_date` date NOT NULL COMMENT '借出日期',
  `expected_return_date` date DEFAULT NULL COMMENT '预计归还日期',
  `actual_return_date` date DEFAULT NULL COMMENT '实际归还日期',
  `status` enum('active','returned','overdue') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '记录状态',
  `notes` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_borrow_asset` (`asset_id`),
  KEY `idx_borrow_borrower` (`borrower_id`),
  KEY `idx_borrow_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产借还记录表';
