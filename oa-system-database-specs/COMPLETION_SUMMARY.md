# OA系统数据库规格文档 - 完成总结

> **基于前端规格**: `oa-system-frontend-specs\`
> **技术栈**: MySQL 8.0+
> **版本**: v1.0.0
> **完成日期**: 2026-01-11

---

## ✅ 已完成的文档清单

### 📁 完整文档结构

```
oa-system-database-specs/
├── README.md                          # 数据库设计总览 ✅
├── DATABASE_SUMMARY.md                # 完整设计总结 ✅
│
├── 01-core/                           # 核心模块
│   ├── employee.md                    # 员工管理 ✅
│   ├── department.md                  # 部门管理 ✅
│   ├── asset.md                       # 资产管理 ✅
│   ├── dict.md                        # 数据字典 ✅
│   └── menu.md                        # 菜单管理 ✅
│
├── 02-approval/                       # 审批模块
│   ├── leave.md                       # 请假审批 ✅
│   └── expense.md                     # 费用报销 ✅
│
├── 03-admin/                          # 管理模块
│   └── meeting.md                     # 会议室管理 ✅
│
├── 04-auth/                           # 认证模块
│   └── login.md                       # 登录认证 ✅
│
└── schemas/                           # SQL脚本
    ├── 01_create_tables.sql           # 建表脚本 ✅ (19张表)
    ├── 02_create_indexes.sql          # 索引脚本 ✅
    ├── 03_create_constraints.sql      # 约束脚本 ✅
    └── 04_init_data.sql               # 初始数据 ✅
```

---

## 📊 数据库设计总览

### 表分类统计

| 表前缀 | 用途 | 表数量 | 表清单 |
|--------|------|--------|--------|
| `sys_` | 系统核心表 | 5 | employee, department, menu, dict_type, dict_item |
| `auth_` | 认证授权表 | 3 | user, user_session, login_log |
| `biz_` | 业务表 | 3 | asset, asset_borrow_record, asset_maintenance |
| `approval_` | 审批表 | 6 | leave_request, leave_approval, leave_balance, expense, expense_item, expense_invoice |
| `admin_` | 管理表 | 2 | meeting_room, meeting_booking |
| **总计** | - | **19** | - |

### 约束实现统计

| 约束类型 | 数量 | 说明 |
|---------|------|------|
| PRIMARY KEY | 19 | 所有表都有主键 |
| FOREIGN KEY | 15 | 外键约束 |
| UNIQUE | 8 | 唯一性约束 |
| CHECK | 25 | 检查约束 |
| INDEX | 60+ | 性能优化索引 |

---

## 🎯 核心特性

### 1. 严格对应前端规格

✅ 每个数据库表都严格对应前端的TypeScript接口
✅ 字段类型、长度、约束完全匹配
✅ 枚举值与前端定义一致
✅ API接口与数据库查询对应

### 2. 完整的约束实现

✅ **实体约束**: 通过验证注解实现
✅ **外键约束**: Service层验证 + 数据库外键
✅ **唯一约束**: @UniqueCheck + Service层验证
✅ **检查约束**: @Pattern/@Min/@Max + 自定义验证
✅ **乐观锁**: @Version 防并发冲突
✅ **逻辑删除**: @TableLogic 软删除

### 3. 性能优化设计

✅ **索引优化**: 60+个索引,覆盖常用查询
✅ **组合索引**: 多字段组合索引优化
✅ **全文索引**: 支持中文全文搜索
✅ **视图**: 10+个视图简化复杂查询
✅ **存储过程**: 封装复杂业务逻辑
✅ **触发器**: 自动计算、状态更新

### 4. 业务规则实现

✅ **自动计算**: 工龄、折旧、时长
✅ **自动提醒**: 生日、转正、超期
✅ **状态管理**: 多级审批、状态流转
✅ **数据校验**: 格式、唯一性、存在性
✅ **审计追踪**: 创建人、更新人、时间戳

---

## 📦 可直接执行的SQL脚本

### 1. 建表脚本 (01_create_tables.sql)

```bash
# 创建所有19张表
mysql -u root -p oa_system < schemas/01_create_tables.sql
```

**包含内容**:
- 5张系统核心表
- 3张认证授权表
- 3张业务表
- 6张审批表
- 2张管理表

### 2. 索引脚本 (02_create_indexes.sql)

```bash
# 创建所有索引
mysql -u root -p oa_system < schemas/02_create_indexes.sql
```

**包含内容**:
- 员工表: 8个索引
- 部门表: 6个索引
- 资产表: 7个索引
- 请假表: 6个索引
- 报销表: 7个索引
- 会议室表: 4个索引
- 其他表索引...

### 3. 约束脚本 (03_create_constraints.sql)

```bash
# 创建所有外键和检查约束
mysql -u root -p oa_system < schemas/03_create_constraints.sql
```

**包含内容**:
- 15个外键约束
- 25个检查约束
- 级联更新/删除规则

### 4. 初始数据脚本 (04_init_data.sql)

```bash
# 初始化基础数据
mysql -u root -p oa_system < schemas/04_init_data.sql
```

**包含内容**:
- 4个示例员工
- 7个示例部门
- 18个菜单项
- 10个字典类型(50+字典项)
- 5个示例资产
- 5个示例会议室
- 4个用户账号
- 示例请假、报销数据

---

## 🔗 数据库约束在后端代码中的实现

### 约束映射关系

| 数据库约束 | Java实现 | 位置 |
|-----------|---------|------|
| PRIMARY KEY | @TableId | Entity |
| FOREIGN KEY | @ExistsCheck + Service验证 | Entity + Service |
| UNIQUE | @UniqueCheck + Service验证 | Entity + Service |
| NOT NULL | @NotNull / @NotBlank | Entity |
| CHECK | @Pattern / 自定义验证方法 | Entity |
| ENUM | Enum类 | Entity.enums |
| 乐观锁 | @Version | Entity |
| 逻辑删除 | @TableLogic | Entity |

### 后端实现示例

```java
// Entity层: 注解验证
@TableName("sys_employee")
public class Employee {
    @TableId(value = "id", type = IdType.INPUT)
    @Pattern(regexp = "^EMP\\d{15}$")
    private String id;

    @Email(message = "邮箱格式不正确")
    @UniqueCheck(field = "email")
    private String email;

    @Version
    private Integer version;

    @TableLogic
    private Boolean isDeleted;
}

// Service层: 业务约束验证
@Override
@Transactional
public void saveEmployee(Employee employee) {
    // 外键约束验证
    Department dept = departmentMapper.selectById(employee.getDepartmentId());
    if (dept == null) {
        throw new BusinessException("部门不存在");
    }

    // 唯一性约束验证
    if (emailExists(employee.getEmail())) {
        throw new BusinessException("邮箱已被使用");
    }

    // 保存
    employeeMapper.insert(employee);
}
```

---

## 🚀 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE oa_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oa_system;
```

### 2. 执行SQL脚本(按顺序)

```bash
# 1. 建表
mysql -u root -p oa_system < schemas/01_create_tables.sql

# 2. 索引
mysql -u root -p oa_system < schemas/02_create_indexes.sql

# 3. 约束
mysql -u root -p oa_system < schemas/03_create_constraints.sql

# 4. 初始数据
mysql -u root -p oa_system < schemas/04_init_data.sql
```

### 3. 验证安装

```sql
-- 检查表是否创建成功
SHOW TABLES;
-- 应显示19张表

-- 检查初始数据
SELECT COUNT(*) FROM sys_employee;          -- 4
SELECT COUNT(*) FROM sys_department;        -- 7
SELECT COUNT(*) FROM sys_menu;              -- 18
SELECT COUNT(*) FROM sys_dict_type;         -- 10
SELECT COUNT(*) FROM auth_user;             -- 4
```

---

## 📚 文档说明

### 每个模块文档包含

1. **数据库表设计**: 完整的CREATE TABLE语句
2. **索引设计**: 优化的索引策略
3. **外键约束**: 数据完整性保障
4. **触发器设计**: 自动化业务逻辑
5. **存储过程**: 封装复杂操作
6. **视图设计**: 简化查询
7. **数据字典**: 字典项初始化
8. **初始数据**: 示例数据
9. **API对应SQL**: 接口映射
10. **业务规则**: 使用说明

### 文档特点

- ✅ **完整性**: 涵盖所有数据库设计要素
- ✅ **可执行**: SQL脚本可直接运行
- ✅ **有注释**: 每个字段都有中文注释
- ✅ **对应前端**: 严格匹配前端接口
- ✅ **考虑后端**: 约束在后端代码中实现

---

## 🎯 数据库设计亮点

### 1. 软删除设计

所有业务表都支持软删除:
```sql
is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除(0否1是)',
deleted_at DATETIME COMMENT '删除时间',
deleted_by VARCHAR(20) COMMENT '删除人ID'
```

### 2. 乐观锁设计

防止并发冲突:
```sql
version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
```

### 3. 审计字段设计

完整的操作追踪:
```sql
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(20),
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
updated_by VARCHAR(20)
```

### 4. JSON字段应用

灵活存储复杂数据:
```sql
attachments JSON COMMENT '附件URL数组'
facilities JSON COMMENT '设施设备'
participant_ids JSON COMMENT '参会人员ID数组'
```

### 5. 枚举类型使用

数据安全性和可读性:
```sql
type ENUM('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity')
status ENUM('draft', 'pending', 'approving', 'approved', 'rejected', 'cancelled')
```

---

## 📖 相关文档

- [前端规格文档](../oa-system-frontend-specs/README.md)
- [后端规格文档](../oa-system-backend-specs/README.md)
- [数据库约束脚本](./schemas/03_create_constraints.sql)

---

## ✅ 完成确认

- ✅ 所有19张数据库表设计完成
- ✅ 所有索引设计完成(60+个)
- ✅ 所有外键约束设计完成(15个)
- ✅ 所有检查约束设计完成(25个)
- ✅ 所有触发器和存储过程设计完成
- ✅ 所有视图设计完成
- ✅ 所有初始数据准备完成
- ✅ SQL脚本可直接执行
- ✅ 文档严格对应前端规格
- ✅ 考虑后端代码实现

---

**项目状态**: 数据库规格文档100%完成
**可用性**: 立即可用于后端开发
**维护性**: 文档完整,注释清晰
**扩展性**: 预留扩展字段和表空间

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
