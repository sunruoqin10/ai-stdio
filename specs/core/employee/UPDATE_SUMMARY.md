# 员工管理模块规范更新说明

> **更新日期**: 2026-01-10
> **更新内容**: 添加数据字典和权限管理集成内容

---

## 📝 更新概述

根据业务需求,已在员工管理模块的功能规范和技术规范中添加了完整的数据字典和权限管理集成内容。

---

## 🔧 功能规范更新 ([employee_Functional.md](employee_Functional.md))

### 新增内容

#### 3.6.3 字段级权限控制
添加了细粒度的字段级权限控制矩阵,明确不同角色对各字段的查看和编辑权限。

#### 3.7 数据字典集成
完整的数据字典集成规范,包括:

**3.7.1 依赖的数据字典类型**
- 员工状态 (`employee_status`)
- 性别 (`gender`)
- 试用期状态 (`probation_status`)
- 职位等级 (`position_level`)
- 部门类型 (`department_type`)
- 员工标签 (`employee_tag`)

**3.7.2 数据字典使用场景**
- 员工状态筛选
- 性别选择器
- 试用期状态显示
- 职级选择

**3.7.3 数据字典初始化要求**
- 模块加载时预加载
- 表单编辑时动态加载
- 字典刷新监听

**3.7.4 数据字典缓存策略**
- 常用字典预加载
- 低频字典按需加载
- 30分钟缓存过期

#### 3.8 权限管理集成
完整的权限管理规范,包括:

**3.8.1 员工管理权限定义**
定义了12个细粒度权限:
- `employee:view` - 查看员工
- `employee:view_all` - 查看所有员工
- `employee:view_department` - 查看本部门员工
- `employee:create` - 新增员工
- `employee:edit` - 编辑员工
- `employee:edit_all` - 编辑所有信息
- `employee:delete` - 删除员工
- `employee:resign` - 办理离职
- `employee:import` - 批量导入
- `employee:export` - 导出列表
- `employee:view_sensitive` - 查看敏感信息
- `employee:reset_password` - 重置密码

**3.8.2 功能权限矩阵**
详细列出了系统管理员、部门管理员、普通员工的功能权限对应关系。

**3.8.3 权限检查实现**
提供了TypeScript代码示例,展示如何在业务逻辑中进行权限检查和数据权限过滤。

**3.8.4 按钮级权限控制**
Vue模板示例,展示如何根据权限显示/隐藏操作按钮。

**3.8.5 字段级权限控制**
敏感字段(薪资、职级等)的权限判断逻辑。

---

## 💻 技术规范更新 ([employee_Technical.md](employee_Technical.md))

### 新增内容

#### 5.3 数据字典集成实现
完整的数据字典实现代码:

**5.3.1 数据字典API封装**
```typescript
// src/modules/dict/api/index.ts
- getDictList() - 获取字典列表
- getDictBatch() - 批量获取字典
- getDictLabel() - 获取字典标签
```

**5.3.2 Pinia字典Store**
```typescript
// src/modules/dict/store/index.ts
- dictData - 字典数据存储
- cacheTime - 缓存时间管理
- loadDicts() - 批量加载字典
- getDictList() - 获取字典列表
- getLabel() - 获取字典标签
- refreshDict() - 刷新字典
```

**5.3.3 员工模块中使用字典**
```typescript
// src/modules/employee/composables/useDict.ts
- useEmployeeDict() - 员工模块字典组合函数
- 预加载常用字典
- 动态加载职级字典
- 获取标签文本转换函数
```

**5.3.4 筛选面板中使用字典**
完整的Vue组件示例,展示如何在筛选面板中使用字典选项。

#### 5.4 权限管理集成实现
完整的权限管理实现代码:

**5.4.1 权限Store扩展**
```typescript
// useEmployeePermission() - 员工权限组合函数
- hasPermission() - 检查权限
- canEditEmployee() - 检查是否可编辑员工
- canViewEmployee() - 检查是否可查看员工
- canViewSensitiveField() - 检查是否可查看敏感字段
- filterEmployeeList() - 数据权限过滤
```

**5.4.2 列表页权限控制**
完整的员工列表页Vue组件示例,包含:
- 操作按钮权限控制
- 数据权限过滤
- 表格操作列权限控制

**5.4.3 详情页权限控制**
完整的员工详情页Vue组件示例,包含:
- 编辑/离职/重置密码按钮权限控制
- 敏感字段(薪资/职级)的显示控制

**5.4.4 表单权限控制**
完整的员工表单Vue组件示例,包含:
- 表单禁用状态控制
- 敏感字段编辑权限控制

**5.4.5 API请求权限拦截**
Axios拦截器扩展,包含:
- 自动添加Token
- 权限审计日志记录
- URL到权限的映射函数

---

## 🎯 使用指南

### 1. 数据字典使用

```typescript
// 在组件中使用字典
import { useEmployeeDict } from '@/modules/employee/composables/useDict'

const {
  statusOptions,      // 员工状态选项
  genderOptions,      // 性别选项
  probationOptions,   // 试用期状态选项
  getStatusLabel,     // 获取状态标签
  getGenderLabel,     // 获取性别标签
  getProbationLabel   // 获取试用期标签
} = useEmployeeDict()

// 在模板中使用
<el-select v-model="form.status">
  <el-option
    v-for="item in statusOptions"
    :key="item.value"
    :label="item.label"
    :value="item.value"
  />
</el-select>

// 显示标签
<span>{{ getStatusLabel(employee.status) }}</span>
```

### 2. 权限检查使用

```typescript
// 在组件中使用权限检查
import { useEmployeePermission } from '@/modules/employee/composables/useEmployeePermission'

const {
  hasPermission,        // 检查权限
  canEditEmployee,      // 检查是否可编辑
  canViewEmployee,      // 检查是否可查看
  filterEmployeeList    // 过滤员工列表
} = useEmployeePermission()

// 权限判断
const canCreate = computed(() => hasPermission('employee:create'))
const canEdit = computed(() => hasPermission('employee:edit'))
const canDelete = computed(() => hasPermission('employee:delete'))

// 在模板中使用
<el-button v-if="canCreate" @click="handleCreate">
  新增员工
</el-button>
```

### 3. 数据权限过滤

```typescript
// 原始数据
const allEmployees = ref<Employee[]>([])

// 应用数据权限过滤
const employees = computed(() => {
  return filterEmployeeList(allEmployees.value)
})
```

---

## 📦 相关文件

### 功能规范
- [employee_Functional.md](employee_Functional.md) - 功能需求规范(已更新)

### 技术规范
- [employee_Technical.md](employee_Technical.md) - 技术实现规范(已更新)

### 其他规范
- [employee_Design.md](employee_Design.md) - UI设计规范(未变更)
- [README.md](README.md) - 模块概览(未变更)

---

## ✅ 检查清单

开发人员在实现员工管理模块时,需要确保:

### 数据字典集成
- [ ] 创建 `src/modules/dict/api/index.ts` - 字典API封装
- [ ] 创建 `src/modules/dict/store/index.ts` - 字典Store
- [ ] 创建 `src/modules/employee/composables/useDict.ts` - 员工字典组合函数
- [ ] 筛选面板使用字典选项
- [ ] 列表和详情页使用字典标签显示

### 权限管理集成
- [ ] 创建 `src/modules/employee/composables/useEmployeePermission.ts` - 权限组合函数
- [ ] 列表页实现按钮级权限控制
- [ ] 列表页实现数据权限过滤
- [ ] 详情页实现敏感字段权限控制
- [ ] 表单实现字段级权限控制
- [ ] API请求添加权限审计

### 权限定义
- [ ] 后端实现12个员工管理权限
- [ ] 角色-权限关联配置
- [ ] 权限缓存和刷新机制

---

## 🚀 下一步

1. **数据字典模块开发**
   - 开发独立的数据字典管理模块
   - 提供字典CRUD功能
   - 实现字典缓存机制

2. **权限管理模块开发**
   - 开发独立的权限管理模块
   - 提供角色-权限配置功能
   - 实现动态权限加载

3. **其他模块更新**
   - 参考员工管理模块的更新方式
   - 为其他业务模块添加数据字典和权限管理内容
   - 保持架构一致性

---

**文档版本**: v1.1.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
