DROP TABLE IF EXISTS `admin_meeting_booking`;
CREATE TABLE `admin_meeting_booking` (
  `id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预定ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议主题',
  `room_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议室ID',
  `room_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会议室名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `booker_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预定人ID',
  `booker_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预定人姓名',
  `participant_count` int DEFAULT NULL COMMENT '参会人数',
  `participant_ids` json DEFAULT NULL COMMENT '参会人员ID数组',
  `agenda` text COLLATE utf8mb4_unicode_ci COMMENT '会议议程',
  `equipment` json DEFAULT NULL COMMENT '所需设备',
  `status` enum('booked','in_progress','completed','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'booked' COMMENT '状态',
  `recurring_rule` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '循环规则',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_booking_room` (`room_id`),
  KEY `idx_booking_time` (`start_time`,`end_time`),
  KEY `idx_booking_booker` (`booker_id`),
  KEY `idx_booking_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室预定表';
