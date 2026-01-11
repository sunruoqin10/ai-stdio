# OA系统数据库规格文档总结

> 本文档总结了完整的OA系统数据库设计,严格基于前端规格规范创建
> 数据库: MySQL 8.0+
> 版本: v1.0.0

---

## 📁 文档结构

```
oa-system-database-specs/
├── README.md                          # 数据库设计总览
├── DATABASE_SUMMARY.md                # 本文档
├── 01-core/                           # 核心模块设计
│   ├── employee.md                    # 员工管理数据库设计
│   ├── department.md                  # 部门管理数据库设计
│   ├── asset.md                       # 资产管理数据库设计
│   ├── dict.md                        # 数据字典数据库设计(待创建)
│   └── menu.md                        # 菜单管理数据库设计(待创建)
├── 02-approval/                       # 审批模块设计
│   ├── leave.md                       # 请假审批数据库设计(待创建)
│   └── expense.md                     # 费用审批数据库设计(待创建)
├── 03-admin/                          # 管理模块设计
│   └── meeting.md                     # 会议室管理数据库设计(待创建)
├── 04-auth/                           # 认证模块设计
│   └── login.md                       # 登录认证数据库设计(待创建)
└── schemas/                           # SQL脚本
    ├── 01_create_tables.sql           # 建表脚本(已完成)
    ├── 02_create_indexes.sql          # 索引脚本(已完成)
    ├── 03_create_constraints.sql      # 约束脚本(已完成)
    └── 04_init_data.sql               # 初始数据(已完成)
```

---

## 🗄️ 数据库表设计汇总

### 表分类统计

| 表前缀 | 用途 | 表数量 | 说明 |
|--------|------|--------|------|
| `sys_` | 系统核心表 | 5 | 员工、部门、菜单、字典等 |
| `auth_` | 认证授权表 | 3 | 用户、会话、登录日志 |
| `biz_` | 业务表 | 3 | 资产及其借还、维护记录 |
| `approval_` | 审批表 | 6 | 请假、报销及其相关 |
| `admin_` | 管理表 | 2 | 会议室及预定 |
| **总计** | - | **19** | - |

---

## 📊 核心表设计

### 1. 系统核心表 (`sys_` 前缀)

#### 1.1 sys_employee (员工表)
- **主键**: VARCHAR(20) - 员工编号
- **关键字段**:
  - 基本信息: 姓名、性别、手机号、邮箱
  - 工作信息: 部门ID、职位、职级、上级ID
  - 状态信息: 员工状态、试用期状态
  - 审计字段: 创建时间、更新时间、软删除
- **索引**:
  - 唯一索引: email, phone
  - 普通索引: department_id, manager_id, status
  - 组合索引: (department_id, status)

#### 1.2 sys_department (部门表)
- **主键**: VARCHAR(20) - 部门编号
- **层级结构**: parent_id 自关联
- **关键字段**:
  - 基本信息: 部门名称、简称
  - 层级关系: 上级ID、负责人ID、层级(1-5)
  - 状态信息: 启用/停用
- **索引**:
  - 唯一索引: (name, parent_id) 同级内唯一
  - 普通索引: parent_id, leader_id, level

#### 1.3 sys_menu (菜单表)
- **主键**: BIGINT AUTO_INCREMENT
- **层级结构**: parent_id 自关联(最多3级)
- **菜单类型**: directory(目录), menu(菜单), button(按钮)
- **关键字段**: 路由路径、组件路径、图标、排序

#### 1.4 sys_dict_type / sys_dict_item (数据字典)
- **类型表**: 字典类型定义
- **项表**: 字典项定义
- **关联**: item.type_code -> type.code

---

### 2. 认证授权表 (`auth_` 前缀)

#### 2.1 auth_user (用户表)
- **主键**: VARCHAR(20) - 关联 sys_employee.id
- **认证信息**: username, password_hash(bcrypt)
- **联系信息**: email, mobile (唯一)
- **状态管理**:
  - status: active/locked/disabled
  - failed_login_attempts: 登录失败次数
  - locked_until: 锁定到期时间
- **安全特性**:
  - 密码哈希: bcrypt
  - 登录限制: 5次失败锁定30分钟
  - 密码过期: 90天强制修改

#### 2.2 auth_user_session (用户会话表)
- **主键**: BIGINT AUTO_INCREMENT
- **会话信息**: JWT Token, Refresh Token
- **设备信息**: 设备类型、设备名称、IP地址、User-Agent
- **过期管理**: expires_at 字段

#### 2.3 auth_login_log (登录日志表)
- **主键**: BIGINT AUTO_INCREMENT
- **日志信息**: 登录状态、失败原因、IP地址、登录地点
- **审计用途**: 追踪登录行为、安全分析

---

### 3. 业务表 (`biz_` 前缀)

#### 3.1 biz_asset (资产表)
- **主键**: VARCHAR(20) - 资产编号
- **资产类别**: electronic, furniture, book, other
- **资产状态**: stock, in_use, borrowed, maintenance, scrapped
- **价值管理**:
  - purchase_price: 购置价格
  - current_value: 当前价值(自动计算折旧)
- **借用信息**: user_id, borrow_date, expected_return_date
- **折旧规则**:
  - 电子设备: 3年, 每年33%
  - 家具: 5年, 每年20%
  - 书籍: 不折旧

#### 3.2 biz_asset_borrow_record (借还记录表)
- **主键**: BIGINT AUTO_INCREMENT
- **记录状态**: active, returned, overdue
- **快照数据**: asset_name, borrower_name (历史记录)
- **超期检查**: 定时任务每天检查

#### 3.3 biz_asset_maintenance (维护记录表)
- **主键**: BIGINT AUTO_INCREMENT
- **维护类型**: repair(修理), maintenance(保养), check(检查)
- **费用记录**: cost 字段

---

### 4. 审批表 (`approval_` 前缀)

#### 4.1 approval_leave_request (请假申请表)
- **主键**: VARCHAR(50) - LEAVE+YYYYMMDD+序号
- **请假类型**:
  - annual: 年假
  - sick: 病假
  - personal: 事假
  - comp_time: 调休
  - marriage: 婚假
  - maternity: 产假
- **审批状态**: draft, pending, approving, approved, rejected, cancelled
- **时长计算**: 支持半天(0.5天)
- **附件支持**: JSON数组存储URL

#### 4.2 approval_leave_approval (审批记录表)
- **主键**: BIGINT AUTO_INCREMENT
- **多级审批**: approval_level (1/2/3)
- **审批状态**: pending, approved, rejected
- **唯一约束**: (request_id, approver_id)

#### 4.3 approval_leave_balance (年假余额表)
- **主键**: BIGINT AUTO_INCREMENT
- **年度管理**: year 字段
- **唯一约束**: (employee_id, year)
- **自动计算**:
  - 工龄 < 1年: 5天
  - 1年 ≤ 工龄 < 10年: 10天
  - 10年 ≤ 工龄 < 20年: 15天
  - 工龄 ≥ 20年: 20天

#### 4.4 approval_expense (费用报销表)
- **主键**: VARCHAR(32) - EXP+YYYYMMDD+序号
- **报销类型**:
  - travel: 差旅费
  - hospitality: 招待费
  - office: 办公费
  - transport: 交通费
  - other: 其他
- **双重审批**:
  - 部门审批: dept_approver_id, dept_approval_status
  - 财务审批: finance_approver_id, finance_approval_status
- **打款管理**: payment_date, payment_proof

#### 4.5 approval_expense_item (费用明细表)
- **主键**: BIGINT AUTO_INCREMENT
- **关联**: expense_id -> expense.id
- **明细信息**: description, amount, expense_date, category

#### 4.6 approval_expense_invoice (发票表)
- **主键**: BIGINT AUTO_INCREMENT
- **发票类型**:
  - vat_special: 增值税专用发票
  - vat_common: 增值税普通发票
  - electronic: 电子发票
- **验真功能**: verified 字段
- **唯一约束**: invoice_number (全国唯一)

---

### 5. 管理表 (`admin_` 前缀)

#### 5.1 admin_meeting_room (会议室表)
- **主键**: VARCHAR(20) - 会议室ID
- **基本信息**: 名称、位置、容纳人数
- **设施设备**: JSON数组存储
- **状态**: available, maintenance, disabled

#### 5.2 admin_meeting_booking (会议预定表)
- **主键**: VARCHAR(50) - 预定ID
- **时间管理**: start_time, end_time (DATETIME)
- **预定状态**: booked, in_progress, completed, cancelled
- **参会人员**: participant_ids JSON数组
- **循环预定**: recurring_rule 字段
- **时间冲突检查**:
  ```sql
  SELECT COUNT(*) FROM admin_meeting_booking
  WHERE room_id = ?
    AND status != 'cancelled'
    AND (
      (start_time <= ? AND end_time > ?)
      OR (start_time < ? AND end_time >= ?)
      OR (start_time >= ? AND end_time <= ?)
    )
  ```

---

## 🔍 索引设计策略

### 索引类型

1. **主键索引** (PRIMARY KEY)
   - 所有表都有主键
   - 使用 InnoDB 自动创建聚簇索引

2. **唯一索引** (UNIQUE)
   - 员工: email, phone
   - 部门: (name, parent_id) 同级唯一
   - 用户: username, email, mobile
   - 发票: invoice_number

3. **普通索引** (INDEX)
   - 外键字段自动创建索引
   - 状态字段: status
   - 时间字段: created_at, updated_at
   - 类型字段: category, type

4. **组合索引** (COMPOSITE)
   - 员工: (department_id, status)
   - 资产: (status, category), (user_id, status)
   - 预定: (start_time, end_time)

5. **全文索引** (FULLTEXT)
   - 部门: (name, short_name)
   - 资产: (name, brand_model)

### 索引优化建议

- ✅ 为常用查询条件创建索引
- ✅ 为 JOIN 字段创建索引
- ✅ 为 ORDER BY 字段创建索引
- ✅ 避免过多索引(影响写入性能)
- ✅ 定期分析慢查询日志
- ✅ 使用 EXPLAIN 分析查询计划

---

## 🔒 约束设计

### 外键约束

```sql
-- 级联更新, 限制删除
ON UPDATE CASCADE
ON DELETE RESTRICT

-- 级联更新, 级联删除
ON UPDATE CASCADE
ON DELETE CASCADE

-- 级联更新, 设置NULL
ON UPDATE CASCADE
ON DELETE SET NULL
```

### 检查约束

```sql
-- 邮箱格式
CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')

-- 手机号格式
CHECK (phone REGEXP '^1[3-9][0-9]{9}$')

-- 数值范围
CHECK (purchase_price >= 0 AND current_value >= 0)
CHECK (duration > 0)
CHECK (capacity > 0)

-- 时间逻辑
CHECK (join_date <= CURDATE())
CHECK (probation_end_date IS NULL OR probation_end_date > join_date)
CHECK (end_time > start_time)
```

---

## ⚙️ 触发器设计

### 自动计算触发器

1. **工龄自动计算**
   - INSERT/UPDATE 时自动计算 work_years
   - 公式: TIMESTAMPDIFF(YEAR, join_date, CURDATE())

2. **资产状态更新**
   - 借出时: status -> 'borrowed'
   - 归还时: status -> 'stock'

3. **部门层级自动计算**
   - 根据父级部门自动计算 level
   - 移动时级联更新子部门层级

---

## 📅 定时任务设计

### 1. 工龄更新任务
- **频率**: 每月1号凌晨1点
- **作用**: 更新所有员工工龄

### 2. 生日提醒任务
- **频率**: 每天早上9点
- **作用**: 检查当天生日的员工并发送祝福

### 3. 转正提醒任务
- **频率**: 每天早上9点
- **作用**: 提前7天提醒试用期到期

### 4. 资产折旧计算
- **频率**: 每月1号凌晨
- **作用**: 重新计算所有资产当前价值

### 5. 超期借用检查
- **频率**: 每天早上9点
- **作用**: 检查超期未归还资产

---

## 🚀 性能优化建议

### 1. 查询优化

```sql
-- 使用覆盖索引
SELECT id, name, status FROM sys_employee
WHERE department_id = ? AND status = 'active';

-- 避免 SELECT *
SELECT id, name, department_id FROM sys_employee;

-- 使用 LIMIT 分页
SELECT * FROM sys_employee LIMIT 20 OFFSET 0;

-- 使用 EXISTS 代替 IN
SELECT * FROM sys_department d
WHERE EXISTS (
  SELECT 1 FROM sys_employee e
  WHERE e.department_id = d.id
);
```

### 2. 分区策略

对于大表(如登录日志),可以考虑按时间分区:

```sql
-- 按月分区
ALTER TABLE auth_login_log
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
  PARTITION p202401 VALUES LESS THAN (202402),
  PARTITION p202402 VALUES LESS THAN (202403),
  -- ...
  PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 3. 读写分离

- 主库: 处理所有写操作
- 从库: 处理读操作
- 中间件: ShardingSphere, MyCat

### 4. 缓存策略

- Redis 缓存热点数据
- 字典数据缓存(30分钟)
- 部门树缓存(5分钟)
- 用户Session缓存

---

## 🔐 安全设计

### 1. 密码安全

- 算法: bcrypt
- 强度: 10轮
- 示例: `$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

### 2. 登录安全

- 失败锁定: 5次失败锁定30分钟
- 验证码: 3次失败后要求验证码
- 双因素认证: 可选功能
- Session管理: JWT Token过期时间2小时

### 3. 数据加密

- 敏感字段: 使用 AES-256 加密
- 传输加密: 强制 HTTPS
- 连接加密: SSL/TLS

### 4. SQL注入防护

- 使用参数化查询
- ORM框架: JPA/MyBatis
- 输入验证: 所有用户输入

---

## 📝 数据库管理

### 1. 备份策略

- 全量备份: 每天凌晨2点
- 增量备份: 每小时
- binlog备份: 实时
- 保留周期: 30天

### 2. 监控指标

- 连接数: 当前活跃连接
- 慢查询: 超过1秒的查询
- 锁等待: 死锁检测
- 磁盘空间: 表空间使用率
- 主从延迟: 秒级延迟告警

### 3. 容量规划

- 预估数据量:
  - 员工: < 10,000
  - 部门: < 1,000
  - 资产: < 50,000
  - 请假记录: < 100,000
  - 报销记录: < 100,000
  - 登录日志: < 10,000,000

---

## 📚 相关文档

- [前端规格文档](../oa-system-frontend-specs/README.md)
- [员工管理前端技术规范](../oa-system-frontend-specs/core/employee/employee_Technical.md)
- [部门管理前端技术规范](../oa-system-frontend-specs/core/department/department_Technical.md)
- [资产管理前端技术规范](../oa-system-frontend-specs/core/asset/asset_Technical.md)
- [请假审批前端技术规范](../oa-system-frontend-specs/approval/leave/leave_Technical.md)
- [费用报销前端技术规范](../oa-system-frontend-specs/approval/expense/expense_Technical.md)
- [会议室管理前端技术规范](../oa-system-frontend-specs/admin/meeting/meeting_Technical.md)
- [菜单管理前端技术规范](../oa-system-frontend-specs/core/menu/menu_Technical.md)
- [数据字典前端技术规范](../oa-system-frontend-specs/core/dict/dict_Technical.md)
- [登录认证前端技术规范](../oa-system-frontend-specs/auth/login/login_Technical.md)

---

## 🎯 快速开始

### 1. 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE oa_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oa_system;
```

### 2. 执行建表脚本

```bash
mysql -u root -p oa_system < schemas/01_create_tables.sql
```

### 3. 执行索引脚本

```bash
mysql -u root -p oa_system < schemas/02_create_indexes.sql
```

### 4. 执行约束脚本

```bash
mysql -u root -p oa_system < schemas/03_create_constraints.sql
```

### 5. 执行初始数据脚本

```bash
mysql -u root -p oa_system < schemas/04_init_data.sql
```

### 6. 验证安装

```sql
-- 检查表是否创建成功
SHOW TABLES;

-- 检查初始数据
SELECT COUNT(*) FROM sys_employee;
SELECT COUNT(*) FROM sys_department;
SELECT COUNT(*) FROM sys_menu;
SELECT COUNT(*) FROM sys_dict_type;
SELECT COUNT(*) FROM sys_dict_item;
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
