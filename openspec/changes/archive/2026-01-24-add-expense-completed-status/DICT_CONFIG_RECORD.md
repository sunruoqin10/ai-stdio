# 数据字典配置记录 - 报销状态"已完成"

**配置日期**: 2026-01-24
**配置人员**: Claude Code
**字典类型**: expense_status (报销状态)

---

## 字典类型信息

```sql
SELECT * FROM sys_dict_type WHERE code = 'expense_status';
```

| 字段 | 值 |
|------|-----|
| id | 10 |
| code | expense_status |
| name | 报销状态 |
| category | business |
| status | enabled |
| item_count | 6 (更新后) |

---

## 新增字典项配置

### 基本信息

| 字段名 | 值 |
|--------|-----|
| **id** | 59 (自动生成) |
| **dict_type_id** | 10 |
| **dict_type_code** | expense_status |
| **type_code** | expense_status |
| **label** | 已完成 |
| **value** | completed |
| **color_type** | success |
| **color** | #67C23A |
| **icon** | (null) |
| **sort_order** | 6 |
| **sort** | 6 |
| **status** | enabled |

---

### SQL 插入语句

```sql
INSERT INTO sys_dict_item (
  dict_type_id,
  dict_type_code,
  type_code,
  label,
  value,
  color_type,
  color,
  sort_order,
  sort,
  status,
  created_at,
  updated_at
) VALUES (
  10,
  'expense_status',
  'expense_status',
  '已完成',
  'completed',
  'success',
  '#67C23A',
  6,
  6,
  'enabled',
  NOW(),
  NOW()
);
```

---

## 验证查询

### 验证是否添加成功
```sql
SELECT
  id,
  label,
  value,
  color_type,
  color,
  sort_order,
  status,
  created_at
FROM sys_dict_item
WHERE type_code = 'expense_status'
ORDER BY sort_order;
```

**预期结果**:

| id | label | value | color_type | color | sort_order | status |
|----|-------|-------|------------|-------|------------|--------|
| 37 | 草稿 | draft | | #909399 | 1 | enabled |
| 38 | 待部门审批 | dept_pending | | #E6A23C | 2 | enabled |
| 39 | 待财务审批 | finance_pending | | #409EFF | 3 | enabled |
| 40 | 已打款 | paid | | #67C23A | 4 | enabled |
| **59** | **已完成** | **completed** | **success** | **#67C23A** | **6** | **enabled** ✨ |
| 41 | 已拒绝 | rejected | | #F56C6C | 7 | enabled |

---

## 前端使用

### API 调用
系统会通过以下方式加载字典数据：

1. **后端 API**: `/api/dict/type/expense_status`
2. **前端 Store**: `dictStore.getDictItems('expense_status')`
3. **动态渲染**: 根据字典数据动态生成下拉选项和标签

### 代码示例

**获取状态选项**:
```typescript
const statusOptions = await dictStore.getDictItems('expense_status')
```

**显示状态标签**:
```vue
<el-tag
  :type="item.color_type"
  :color="item.color"
>
  {{ item.label }}
</el-tag>
```

---

## 维护说明

### 修改记录
- **2026-01-24**: 新增"已完成"状态
- **维护人员**: 财务模块团队

### 注意事项
1. ✅ **不要删除**: 此字典项为系统核心数据，请勿删除
2. ✅ **不要修改 value**: `completed` 是业务逻辑硬编码的值
3. ✅ **可修改**: label、color 可以根据需要调整
4. ✅ **排序**: 保持 sort_order 为 6（在所有状态之后）

### 相关表
- `expense` 表的 `status` 字段使用此字典
- 前端类型定义 `ExpenseStatus` 必须与此同步

---

**文档版本**: 1.0
**最后更新**: 2026-01-24
**审核状态**: 待审核
