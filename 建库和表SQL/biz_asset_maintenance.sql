DROP TABLE IF EXISTS `biz_asset_maintenance`;
CREATE TABLE `biz_asset_maintenance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `asset_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资产ID',
  `type` enum('repair','maintenance','check') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '费用',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `operator_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_maint_asset` (`asset_id`),
  KEY `idx_maint_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产维护记录表';
