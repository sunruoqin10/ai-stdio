DROP TABLE IF EXISTS `admin_meeting_room`;
CREATE TABLE `admin_meeting_room` (
  `id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议室ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议室名称',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '位置',
  `capacity` int NOT NULL COMMENT '容纳人数',
  `facilities` json DEFAULT NULL COMMENT '设施设备',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `status` enum('available','maintenance','disabled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'available' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室表';
