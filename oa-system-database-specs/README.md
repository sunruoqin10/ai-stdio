# OA系统数据库设计规格文档

> 本文档基于前端规格规范 `oa-system-frontend-specs\` 严格对应设计
> 数据库: MySQL 8.0+

## 文档结构

```
oa-system-database-specs/
├── README.md                          # 数据库设计总览
├── 01-core/                           # 核心模块
│   ├── employee.md                    # 员工管理
│   ├── department.md                  # 部门管理
│   ├── asset.md                       # 资产管理
│   ├── dict.md                        # 数据字典
│   └── menu.md                        # 菜单管理
├── 02-approval/                       # 审批模块
│   ├── leave.md                       # 请假审批
│   └── expense.md                     # 费用审批
├── 03-admin/                          # 管理模块
│   └── meeting.md                     # 会议室管理
├── 04-auth/                           # 认证模块
│   └── login.md                       # 登录认证
├── schemas/                           # SQL脚本
│   ├── 01_create_tables.sql           # 建表脚本
│   ├── 02_create_indexes.sql          # 索引脚本
│   ├── 03_create_constraints.sql      # 约束脚本
│   └── 04_init_data.sql               # 初始数据
└── erd/                               # 数据库关系图
    └── oa-system-erd.png              # 实体关系图
```

## 数据库命名规范

### 表命名
- 全部小写，使用下划线分隔
- 格式: `模块_实体` (如: `sys_employee`, `biz_leave_request`)
- 系统表前缀: `sys_`
- 业务表前缀: `biz_`

### 字段命名
- 使用下划线分隔的小写单词
- 主键: `id`
- 外键: `关联表_id` (如: `department_id`)
- 时间字段: `created_at`, `updated_at`
- 状态字段: `status`
- 布尔字段: `is_` 前缀 (如: `is_active`)

### 索引命名
- 主键索引: `PRIMARY`
- 唯一索引: `uk_表名_字段名`
- 普通索引: `idx_表名_字段名`
- 全文索引: `ft_表名_字段名`

## 数据类型映射

| TypeScript类型 | MySQL类型 | 说明 |
|---|---|---|
| string | VARCHAR(n) | 变长字符串 |
| string (ID) | VARCHAR(20) | 主键/外键ID |
| string (枚举) | ENUM | 枚举值 |
| number | INT / BIGINT | 整数 |
| number (小数) | DECIMAL(m,d) | 精确小数 |
| boolean | TINYINT(1) | 0/1 |
| Date | DATETIME | 日期时间 |
| string[] | JSON | 数组转JSON |
| object | JSON | 对象转JSON |

## 数据库设计原则

### 1. 范式化设计
- 第一范式(1NF): 字段原子性
- 第二范式(2NF): 完全依赖主键
- 第三范式(3NF): 消除传递依赖

### 2. 性能优化
- 合理使用索引
- 避免过度范式化
- 读写分离考虑
- 分表分库预留

### 3. 扩展性
- 预留扩展字段
- 使用JSON存储动态属性
- 软删除设计
- 多租户支持预留

### 4. 数据完整性
- 主外键约束
- 唯一性约束
- 检查约束
- 触发器保障

## 全局字段设计

### 通用审计字段
所有业务表均包含以下字段：

```sql
created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
created_by       VARCHAR(20)    COMMENT '创建人ID',
updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
updated_by       VARCHAR(20)    COMMENT '更新人ID',
is_deleted       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
deleted_at       DATETIME       COMMENT '删除时间',
deleted_by       VARCHAR(20)    COMMENT '删除人ID',
version          INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
```

### 状态字段通用值
- 启用状态: `enabled`/`disabled` 或 `active`/`inactive`
- 审核状态: `pending`/`approved`/`rejected`
- 删除状态: `0`(未删除) / `1`(已删除)

## 字符集与排序规则

```sql
-- 数据库字符集
CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci
```

## 数据库版本管理

使用Flyway或Liquibase进行版本管理：

```
V1.0.0__init_schema.sql
V1.0.1__add_employee_table.sql
V1.0.2__add_department_table.sql
...
```

## 数据库部署

### 开发环境
- 单实例部署
- 定期备份
- 慢查询日志开启

### 生产环境
- 主从复制
- 读写分离
- 定时备份
- 监控告警
- 高可用架构

## 相关文档

- [员工管理数据库设计](./01-core/employee.md)
- [部门管理数据库设计](./01-core/department.md)
- [资产管理数据库设计](./01-core/asset.md)
- [数据字典数据库设计](./01-core/dict.md)
- [菜单管理数据库设计](./01-core/menu.md)
- [请假审批数据库设计](./02-approval/leave.md)
- [费用审批数据库设计](./02-approval/expense.md)
- [会议室管理数据库设计](./03-admin/meeting.md)
- [登录认证数据库设计](./04-auth/login.md)
