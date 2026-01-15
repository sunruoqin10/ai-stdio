# 前后端适配说明文档

## 概述
本文档说明前端部门模块与后端API的适配情况。

## 后端API基础信息

### Base URL
```
/api
```

### 认证方式
```
Authorization: Bearer {token}
```

## API端点映射

### 1. 部门列表

#### 后端接口
```
GET /api/departments?page={page}&pageSize={pageSize}&keyword={keyword}&status={status}&leaderId={leaderId}&level={level}
```

#### 响应格式 (MyBatis Plus分页)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "size": 20
  }
}
```

#### 前端适配
- 文件: `oa-system-frontend/src/modules/department/api/index.ts`
- 函数: `getList()`
- 转换: MyBatis Plus格式 → 前端PaginationResponse格式

### 2. 部门树

#### 后端接口
```
GET /api/departments/tree
```

#### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "DEPT0001",
      "name": "总部",
      "parentId": null,
      "parentName": null,
      "leaderId": "EMP0001",
      "leaderName": "张三",
      "level": 1,
      "children": [...]
    }
  ]
}
```

### 3. 部门详情

#### 后端接口
```
GET /api/departments/{id}
```

#### 响应字段说明
```json
{
  "code": 200,
  "data": {
    "id": "DEPT0001",
    "name": "技术部",
    "shortName": "技术部",
    "parentId": "DEPT0001",
    "parentName": "总部",
    "leaderId": "EMP0001",
    "leaderName": "张三",
    "level": 2,
    "sort": 1,
    "establishedDate": "2020-01-01",
    "description": "负责技术研发",
    "icon": null,
    "status": "active",
    "employeeCount": 10,
    "childCount": 3,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "version": 1
  }
}
```

### 4. 创建部门

#### 后端接口
```
POST /api/departments
```

#### 请求体
```json
{
  "name": "新部门",
  "shortName": "新部门",
  "parentId": "DEPT0001",
  "leaderId": "EMP0001",
  "sort": 1,
  "establishedDate": "2024-01-01",
  "description": "部门描述",
  "icon": "icon-url"
}
```

### 5. 更新部门

#### 后端接口
```
PUT /api/departments/{id}
```

#### 请求体
```json
{
  "name": "更新后的名称",
  "shortName": "简称",
  "leaderId": "EMP0001",
  "sort": 2,
  "establishedDate": "2024-01-01",
  "description": "更新描述",
  "icon": "icon-url",
  "status": "active",
  "version": 1
}
```

**注意**: `version` 字段用于乐观锁，需要从部门详情获取最新值。

### 6. 移动部门

#### 后端接口
```
PUT /api/departments/{id}/move
```

#### 请求体
```json
{
  "newParentId": "DEPT0002",
  "version": 1
}
```

### 7. 删除部门

#### 后端接口
```
DELETE /api/departments/{id}
```

### 8. 批量删除

#### 后端接口
```
DELETE /api/departments/batch
```

#### 请求体
```json
["DEPT0001", "DEPT0002", "DEPT0003"]
```

#### 响应格式
```json
{
  "code": 200,
  "data": {
    "total": 3,
    "success": 2,
    "failed": 1,
    "errors": [
      {
        "id": "DEPT0003",
        "message": "部门下存在成员，无法删除"
      }
    ]
  }
}
```

### 9. 部门成员

#### 后端接口
```
GET /api/departments/{id}/members
```

#### 响应格式
```json
{
  "code": 200,
  "data": [
    {
      "employeeId": "EMP0001",
      "employeeName": "张三",
      "employeeAvatar": "avatar-url",
      "position": "软件工程师",
      "status": "active",
      "isLeader": true,
      "joinDepartmentDate": "2024-01-01"
    }
  ]
}
```

### 10. 部门统计

#### 后端接口
```
GET /api/departments/statistics
```

#### 响应格式
```json
{
  "code": 200,
  "data": {
    "totalCount": 50,
    "level1Count": 5,
    "level2Count": 20,
    "level3Count": 15,
    "level4Count": 10,
    "maxLevel": 4,
    "withLeaderCount": 45,
    "totalEmployees": 200,
    "activeDepartmentCount": 48,
    "disabledDepartmentCount": 2
  }
}
```

### 11. 检查接口

#### 检查名称是否存在
```
GET /api/departments/check-name?name={name}&parentId={parentId}&excludeId={excludeId}
```

#### 检查是否有子部门
```
GET /api/departments/{id}/has-children
```

#### 检查是否有成员
```
GET /api/departments/{id}/has-members
```

## 数据字段映射

### 核心变化

| 前端字段 (旧) | 后端字段 | 前端字段 (新) | 说明 |
|---|---|---|---|
| `leader.id` | `leaderId` | `leaderId` | 负责人ID |
| `leader.name` | `leaderName` | `leaderName` | 负责人姓名 |
| `leader.avatar` | (无) | (删除) | 负责人头像不再返回 |
| - | `parentName` | `parentName` | 新增：上级部门名称 |
| - | `childCount` | `childCount` | 新增：子部门数量 |
| - | `version` | `version` | 新增：乐观锁版本号 |

### Department接口完整字段

```typescript
interface Department {
  id: string                      // 部门ID
  name: string                    // 部门名称
  shortName?: string              // 部门简称
  parentId?: string | null        // 上级部门ID
  parentName?: string             // 上级部门名称(关联查询)
  leaderId?: string               // 部门负责人ID
  leaderName?: string             // 部门负责人名称(关联查询)
  level: number                   // 部门层级
  sort: number                    // 排序号
  establishedDate?: string        // 成立时间
  description?: string            // 部门描述
  icon?: string                   // 部门图标URL
  status: DepartmentStatus        // 状态
  createdAt: string               // 创建时间
  updatedAt: string               // 更新时间
  children?: Department[]         // 子部门列表
  employeeCount?: number          // 员工数量
  childCount?: number             // 子部门数量
  version?: number                // 乐观锁版本号
}
```

## 前端组件适配

### 1. 视图组件 (views/index.vue)

#### 变更点
- 负责人显示从 `row.leader.name` 改为 `row.leaderName`
- 删除了负责人头像显示（后端不再返回）
- 新增 `parentName` 字段支持
- 新增 `childCount` 字段支持

#### 修改前
```vue
<el-table-column prop="leader" label="负责人" width="120">
  <template #default="{ row }">
    <div v-if="row.leader" class="leader-info">
      <el-avatar :size="24" :src="row.leader.avatar">
        {{ row.leader.name.charAt(0) }}
      </el-avatar>
      <span>{{ row.leader.name }}</span>
    </div>
    <span v-else class="text-placeholder">未设置</span>
  </template>
</el-table-column>
```

#### 修改后
```vue
<el-table-column prop="leaderName" label="负责人" width="120">
  <template #default="{ row }">
    <span v-if="row.leaderName" class="leader-name">{{ row.leaderName }}</span>
    <span v-else class="text-placeholder">未设置</span>
  </template>
</el-table-column>
```

### 2. API层 (api/index.ts)

#### 完全重写，连接真实后端API
- 移除所有mock数据调用
- 使用 `http` 工具调用后端接口
- 添加MyBatis Plus分页格式转换
- 添加字段映射转换

### 3. 类型定义 (types/index.ts)

#### Department接口更新
- 移除嵌套的 `leader` 对象
- 新增扁平字段: `parentName`, `leaderName`, `childCount`, `version`

### 4. Store层 (store/index.ts)

#### loadList方法更新
```typescript
async function loadList(type: 'tree' | 'flat' = 'flat', page: number = 1, pageSize: number = 1000) {
  loading.value = true
  try {
    if (type === 'tree') {
      // 调用树形API
      const data = await api.getDepartmentTree()
      tree.value = data
      list.value = flattenTree(data)
      return data
    } else {
      // 调用分页API，获取所有数据
      const data = await api.getList({
        ...filter.value,
        page,
        pageSize
      })

      list.value = data.list
      tree.value = buildTree(data.list) as Department[]
      return data
    }
  } finally {
    loading.value = false
  }
}
```

## 待完成事项

### 1. 版本号处理
**问题**: 更新和移动部门需要传递 `version` 字段用于乐观锁

**解决方案**:
- 在编辑/移动操作前，先调用 `getDetail()` 获取最新版本号
- 在表单组件中存储版本号
- 提交时包含版本号

### 2. 导出功能
**状态**: 后端API端点未实现

**临时方案**: 前端继续使用mock导出

**待实现**: 后端需要实现 `/api/departments/export` 接口

### 3. 错误处理
**需要加强**:
- 统一错误提示
- 乐观锁冲突提示
- 网络异常处理

### 4. 缓存策略
**后端**: 使用Caffeine缓存
- 部门列表缓存: 5分钟
- 部门详情缓存: 10分钟
- 统计信息缓存: 1分钟

**前端建议**:
- 实现本地缓存机制
- 减少不必要的API调用
- 在更新/删除操作后清除缓存

## 测试清单

### 功能测试
- [ ] 部门列表加载
- [ ] 部门树形展示
- [ ] 创建部门
- [ ] 编辑部门
- [ ] 移动部门
- [ ] 删除部门
- [ ] 批量删除
- [ ] 搜索部门
- [ ] 部门详情查看
- [ ] 部门成员查看
- [ ] 统计数据显示

### 边界测试
- [ ] 乐观锁冲突处理
- [ ] 删除有子部门的部门
- [ ] 删除有成员的部门
- [ ] 移动到子部门下
- [ ] 循环引用检测
- [ ] 网络异常处理

### 性能测试
- [ ] 大量部门数据加载
- [ ] 树形结构渲染性能
- [ ] 缓存命中测试

## 部署说明

### 后端部署
1. 确保数据库表 `sys_department` 已创建
2. 确保数据库表 `sys_employee` 已创建（部门成员查询需要）
3. 配置Caffeine缓存参数
4. 启动Spring Boot应用

### 前端部署
1. 更新API配置中的baseURL
2. 确保认证token正常传递
3. 构建生产版本
4. 部署到Web服务器

## 备注
- 所有时间字段使用ISO 8601格式
- 分页从1开始（不是0）
- 软删除使用 `is_deleted` 字段
- 乐观锁使用 `version` 字段
