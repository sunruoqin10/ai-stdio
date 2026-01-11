# 数据字典数据库设计

> **对应前端规范**: [dict_Technical.md](../../../oa-system-frontend-specs/core/dict/dict_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `sys_`
> **版本**: v1.0.0

---

## 数据库表设计

完整的SQL已在前端规格中定义,此处补充业务说明。

### 表结构概览

| 表名 | 说明 | 记录数 |
|------|------|--------|
| sys_dict_type | 字典类型表 | ~50 |
| sys_dict_item | 字典项表 | ~500 |

### 已初始化的字典类型

1. **gender** - 性别
2. **employee_status** - 员工状态
3. **probation_status** - 试用期状态
4. **department_status** - 部门状态
5. **asset_category** - 资产类别
6. **asset_status** - 资产状态
7. **leave_type** - 请假类型
8. **leave_status** - 请假状态
9. **expense_type** - 报销类型
10. **expense_status** - 报销状态

### 使用规范

- 字典数据通过 `04_init_data.sql` 初始化
- 运行时可通过管理界面维护
- 字典缓存30分钟自动刷新

**文档版本**: v1.0.0
**最后更新**: 2026-01-11
