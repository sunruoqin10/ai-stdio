DROP TABLE IF EXISTS `approval_holiday`;
CREATE TABLE `approval_holiday` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `date` date NOT NULL COMMENT '日期',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节假日名称',
  `type` enum('national','company') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'national' COMMENT '类型:国家/公司',
  `year` int NOT NULL COMMENT '年份',
  `is_workday` tinyint(1) DEFAULT '0' COMMENT '是否为工作日(调休)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `date` (`date`),
  KEY `idx_holiday_year` (`year`),
  KEY `idx_holiday_date` (`date`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节假日表';
