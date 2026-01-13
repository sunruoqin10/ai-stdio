DROP TABLE IF EXISTS `sys_department_member`;
CREATE TABLE `sys_department_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `department_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门ID',
  `employee_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工ID',
  `is_leader` tinyint(1) DEFAULT '0' COMMENT '是否为负责人(0否1是)',
  `join_date` date NOT NULL COMMENT '加入部门日期',
  `leave_date` date DEFAULT NULL COMMENT '离开部门日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_member` (`department_id`,`employee_id`,`leave_date`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门成员关系表';
