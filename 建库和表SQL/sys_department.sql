DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门编号(格式: DEPT+4位序号)',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `short_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门简称',
  `parent_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上级部门ID(NULL表示顶级)',
  `leader_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门负责人ID',
  `level` int NOT NULL DEFAULT '1' COMMENT '部门层级(从1开始)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `established_date` date DEFAULT NULL COMMENT '成立日期',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '部门描述',
  `icon` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门图标URL',
  `status` enum('active','disabled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除(0否1是)',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  `deleted_by` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '删除人ID',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';
