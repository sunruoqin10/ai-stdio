# 员工模块数据字典集成文档

## 概述

员工管理模块已完整集成数据字典功能，实现了以下字段的字典化管理：
- 性别 (employee_gender)
- 员工状态 (employee_status)
- 试用期状态 (employee_probation_status)
- 职位 (employee_position)
- 职级 (employee_level)

## 架构说明

### 1. 数据库层

#### 字典类型表 (sys_dict_type)
存储字典类型的定义，包括编码、名称、描述、分类等。

#### 字典项表 (sys_dict_item)
存储具体的字典项，包括标签、值、颜色、图标等。

#### SQL 初始化脚本
位置：[employee_dict_data.sql](./employee_dict_data.sql)

该脚本包含 5 个员工相关字典类型的完整数据：
1. **employee_gender**: 性别 (男、女)
2. **employee_status**: 员工状态 (在职、离职、停薪留职)
3. **employee_probation_status**: 试用期状态 (试用期内、已转正、已离职)
4. **employee_position**: 职位 (12种职位选项)
5. **employee_level**: 职级 (8种职级选项)

### 2. 后端实现

#### 字典服务 (DictService)
- 位置: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/dict/service/DictService.java`
- 实现: `DictServiceImpl.java`
- 主要方法:
  - `getDictData(String dictTypeCode)`: 获取指定类型的字典数据（带缓存）
  - `clearDictCache(String dictTypeCode)`: 清除字典缓存

#### 员工控制器 (EmployeeController)
- 位置: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/employee/controller/EmployeeController.java`
- API 端点: `GET /api/employees/dict-data`
- 返回所有员工相关的字典数据

```java
@GetMapping("/dict-data")
public ApiResponse<Map<String, DictDataVO>> getEmployeeDictData() {
    Map<String, DictDataVO> dictData = new HashMap<>();
    dictData.put("gender", dictService.getDictData("employee_gender"));
    dictData.put("status", dictService.getDictData("employee_status"));
    dictData.put("probationStatus", dictService.getDictData("employee_probation_status"));
    dictData.put("position", dictService.getDictData("employee_position"));
    dictData.put("level", dictService.getDictData("employee_level"));
    return ApiResponse.success(dictData);
}
```

#### 字典标签工具 (DictLabelUtil)
- 位置: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/employee/util/DictLabelUtil.java`
- 功能: 自动为 EmployeeVO 和 EmployeeDetailVO 填充字典标签
- 使用场景: 员工列表和详情查询时自动转换

```java
// 自动填充字典标签
dictLabelUtil.fillDictLabelsForList(result.getRecords());  // 列表
dictLabelUtil.fillDictLabels(vo);  // 单个对象
```

#### 员工服务 (EmployeeServiceImpl)
- 位置: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/employee/service/impl/EmployeeServiceImpl.java`
- 集成点:
  - Line 56: 员工列表查询后自动填充字典标签
  - Line 84: 员工详情查询后自动填充字典标签

### 3. 前端实现

#### API 接口 (employee/api/index.ts)
- 位置: `oa-system-frontend/src/modules/employee/api/index.ts`
- 类型定义: `EmployeeDictData` 和 `DictData`
- API 方法: `getEmployeeDictData()`

```typescript
export interface EmployeeDictData {
  gender: DictData
  status: DictData
  probationStatus: DictData
  position: DictData
  level: DictData
}

export async function getEmployeeDictData(): Promise<EmployeeDictData> {
  const response = await http.get<{
    code: number
    message: string
    data: EmployeeDictData
  }>('/employees/dict-data')
  return response.data
}
```

#### 员工表单 (EmployeeForm.vue)
- 位置: `oa-system-frontend/src/modules/employee/components/EmployeeForm.vue`
- 集成点:
  - Line 244: 定义 `dictData` 响应式变量
  - Line 307: 在 `onMounted` 中加载字典数据
  - Line 27-32: 性别选择器使用字典数据
  - Line 87-91: 职位选择器使用字典数据
  - Line 398-404: `getDictLabel()` 方法用于显示字典标签

```vue
<!-- 性别选择示例 -->
<el-radio-group v-model="formData.gender">
  <el-radio
    v-for="item in dictData?.gender.items"
    :key="item.value"
    :value="item.value"
  >
    {{ item.label }}
  </el-radio>
</el-radio-group>

<!-- 职位选择示例 -->
<el-select v-model="formData.position">
  <el-option
    v-for="item in dictData?.position.items"
    :key="item.value"
    :label="item.label"
    :value="item.value"
  />
</el-select>
```

#### 员工列表 (EmployeeList.vue)
- 位置: `oa-system-frontend/src/modules/employee/views/EmployeeList.vue`
- 显示字典标签:
  - Line 78: 职位显示 `positionLabel`
  - Line 84: 试用期状态显示 `probationStatusLabel`
  - Line 89: 员工状态显示 `statusLabel`

```vue
<!-- 表格列显示字典标签 -->
<el-table-column label="职位">
  <template #default="{ row }">
    {{ row.positionLabel || row.position }}
  </template>
</el-table-column>

<!-- 使用状态标签组件 -->
<el-table-column label="状态">
  <template #default="{ row }">
    <StatusTag :status="row.status" :label="row.statusLabel" />
  </template>
</el-table-column>
```

#### 类型定义 (types/index.ts)
- 位置: `oa-system-frontend/src/modules/employee/types/index.ts`
- Employee 接口包含字典标签字段:
  - `genderLabel`: 性别显示名称
  - `positionLabel`: 职位显示名称
  - `levelLabel`: 职级显示名称
  - `probationStatusLabel`: 试用期状态显示名称
  - `statusLabel`: 员工状态显示名称

## 使用流程

### 初始化数据

1. **执行 SQL 脚本**
```bash
mysql -u root -p oa_system < employee_dict_data.sql
```

2. **验证数据**
```sql
-- 查看字典类型
SELECT * FROM sys_dict_type
WHERE code IN ('employee_gender', 'employee_status', 'employee_probation_status', 'employee_position', 'employee_level');

-- 查看字典项
SELECT dt.code AS dict_type, di.label, di.value
FROM sys_dict_item di
JOIN sys_dict_type dt ON di.type_code = dt.code
WHERE dt.code IN ('employee_gender', 'employee_status', 'employee_probation_status', 'employee_position', 'employee_level')
ORDER BY dt.sort_order, di.sort_order;
```

### 前端使用

1. **组件中加载字典数据**
```typescript
import * as employeeApi from '../api'

const dictData = ref<employeeApi.EmployeeDictData | null>(null)

onMounted(async () => {
  dictData.value = await employeeApi.getEmployeeDictData()
})
```

2. **表单中使用字典**
```vue
<el-select v-model="formData.position">
  <el-option
    v-for="item in dictData?.position.items"
    :key="item.value"
    :label="item.label"
    :value="item.value"
  />
</el-select>
```

3. **显示字典标签**
```vue
<template #default="{ row }">
  {{ getDictLabel('position', row.position) }}
</template>

<script setup>
function getDictLabel(dictType: keyof employeeApi.EmployeeDictData, value: string) {
  if (!dictData.value || !value) return value || '-'
  const items = dictData.value[dictType]?.items
  if (!items) return value
  const item = items.find(i => i.value === value)
  return item?.label || value
}
</script>
```

### 后端使用

后端会自动通过 `DictLabelUtil` 为查询结果填充字典标签，无需手动处理。

## 数据字典缓存

字典数据使用 Redis 缓存，提高查询性能：
- 缓存 Key: `dict:data:{dictTypeCode}`
- 过期时间: 30 分钟
- 更新字典后自动清除对应缓存

## 扩展字典

如需添加新的字典类型：

1. **数据库层面**: 在 `sys_dict_type` 和 `sys_dict_item` 表中添加数据
2. **后端层面**: 在 `EmployeeController.getEmployeeDictData()` 方法中添加新的字典查询
3. **前端层面**:
   - 在 `api/index.ts` 的 `EmployeeDictData` 接口中添加字段
   - 在组件中加载和使用新字典

## 测试验证

### 1. 测试字典数据 API
```bash
curl http://localhost:8080/api/employees/dict-data
```

期望返回包含 5 个字典类型的完整数据。

### 2. 测试员工列表
访问员工列表页面，验证：
- 性别显示正确
- 职位显示中文名称而非代码
- 状态标签显示正确且带颜色

### 3. 测试员工表单
创建/编辑员工时验证：
- 性别单选框显示"男"、"女"
- 职位下拉框显示所有职位选项
- 职级下拉框显示所有职级选项

## 注意事项

1. **字典编码规范**: 员工相关字典编码以 `employee_` 开头
2. **值与标签分离**:
   - 数据库存储 `value` (英文代码)
   - 前端显示 `label` (中文名称)
3. **颜色主题**: 可在字典项中配置 `colorType` 和 `color`，前端 StatusTag 组件会自动应用
4. **排序**: 字典项按 `sort_order` 字段排序显示
5. **缓存更新**: 修改字典数据后，缓存会自动清除，无需手动处理

## 相关文件清单

### 后端
- [DictService.java](../../../../src/main/java/com/example/oa_system_backend/module/dict/service/DictService.java)
- [DictServiceImpl.java](../../../../src/main/java/com/example/oa_system_backend/module/dict/service/impl/DictServiceImpl.java)
- [DictDataVO.java](../../../../src/main/java/com/example/oa_system_backend/module/dict/vo/DictDataVO.java)
- [DictItemVO.java](../../../../src/main/java/com/example/oa_system_backend/module/dict/vo/DictItemVO.java)
- [EmployeeController.java](../../../../src/main/java/com/example/oa_system_backend/module/employee/controller/EmployeeController.java)
- [EmployeeServiceImpl.java](../../../../src/main/java/com/example/oa_system_backend/module/employee/service/impl/EmployeeServiceImpl.java)
- [DictLabelUtil.java](../../../../src/main/java/com/example/oa_system_backend/module/employee/util/DictLabelUtil.java)

### 前端
- [api/index.ts](../../../../../oa-system-frontend/src/modules/employee/api/index.ts)
- [types/index.ts](../../../../../oa-system-frontend/src/modules/employee/types/index.ts)
- [components/EmployeeForm.vue](../../../../../oa-system-frontend/src/modules/employee/components/EmployeeForm.vue)
- [views/EmployeeList.vue](../../../../../oa-system-frontend/src/modules/employee/views/EmployeeList.vue)
- [views/EmployeeDetail.vue](../../../../../oa-system-frontend/src/modules/employee/views/EmployeeDetail.vue)

### 数据库
- [employee_dict_data.sql](./employee_dict_data.sql)

## 总结

员工模块已完全集成数据字典功能，实现了：
- ✅ 数据库字典表结构完整
- ✅ 后端 API 端点已实现
- ✅ 前端组件已集成字典数据
- ✅ 自动填充字典标签
- ✅ 缓存机制优化性能
- ✅ 易于扩展和维护

只需执行 SQL 脚本初始化数据，即可立即使用字典功能。
