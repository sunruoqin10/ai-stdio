DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单编号(MENU+4位序号)',
  `menu_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_type` enum('directory','menu','button') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单类型',
  `parent_id` bigint DEFAULT '0' COMMENT '父级菜单ID(0表示一级菜单)',
  `menu_level` int NOT NULL DEFAULT '1' COMMENT '菜单层级(1-3)',
  `route_path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由路径',
  `component_path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单图标',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `visible` tinyint(1) DEFAULT '1' COMMENT '是否可见(0否1是)',
  `status` enum('enabled','disabled') COLLATE utf8mb4_unicode_ci DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `menu_code` (`menu_code`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单管理表';
