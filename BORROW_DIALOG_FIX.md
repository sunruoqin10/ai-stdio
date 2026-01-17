# 修复资产借用功能 - 员工列表加载问题

## 问题描述

点击【确认借出】按钮时，后端返回错误：**"借用人不存在"**

### 根本原因

前端 `BorrowDialog.vue` 组件使用的是**硬编码的模拟员工数据**：
```typescript
const userList = ref<User[]>([
  { id: 'EMP000001', name: '张三', avatar: '...' },
  { id: 'EMP000002', name: '李四', avatar: '...' },
  // ...
])
```

这些员工ID在数据库中**不存在**，导致后端验证失败。

## 解决方案

### 1. 从后端API获取真实员工数据

修改 `BorrowDialog.vue`，添加员工列表加载逻辑：

#### 导入员工API和类型
```typescript
import { getEmployeeList } from '@/modules/employee/api'
import { EmployeeStatus } from '@/modules/employee/types'
```

#### 加载员工列表函数
```typescript
const loadingUsers = ref(false)
const userList = ref<User[]>([])

async function loadUserList() {
  loadingUsers.value = true
  try {
    const result = await getEmployeeList({
      page: 1,
      pageSize: 100,
      status: EmployeeStatus.ACTIVE // 只获取在职员工
    })

    userList.value = result.list.map(emp => ({
      id: emp.id,
      name: emp.name,
      avatar: emp.avatar || 'https://i.pravatar.cc/150?img=1'
    }))
  } catch (error: any) {
    console.error('加载员工列表失败:', error)
    ElMessage.error('加载员工列表失败')
    userList.value = []
  } finally {
    loadingUsers.value = false
  }
}
```

#### 组件挂载时加载
```typescript
onMounted(() => {
  loadUserList()
})
```

#### 更新UI显示加载状态
```vue
<el-select
  v-model="form.borrowerId"
  placeholder="请选择借用人"
  :loading="loadingUsers"
>
  <el-option
    v-for="user in userList"
    :key="user.id"
    :label="user.name"
    :value="user.id"
  >
    ...
  </el-option>
  <el-option v-if="!loadingUsers && userList.length === 0" disabled value="">
    暂无可用的员工
  </el-option>
</el-select>
```

## 测试步骤

### 1. 确认数据库中有员工数据

使用MCP查询：
```sql
SELECT id, name, status FROM sys_employee WHERE is_deleted = 0 LIMIT 10;
```

### 2. 启动后端服务

```bash
cd oa-system-backend
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
cd oa-system-frontend
npm run dev
```

### 4. 测试借用流程

1. 登录系统
2. 进入资产管理页面
3. 选择一个"在库"状态的资产
4. 点击【借用】按钮
5. 查看借用人下拉框：
   - 应该显示加载动画
   - 加载完成后显示真实的员工列表
6. 选择一个真实的员工
7. 选择借出日期和预计归还日期
8. 点击【确认借出】
9. 检查是否成功

## 预期结果

✅ **借用人下拉框**显示数据库中真实的在职员工
✅ **借用成功**后，资产状态变为"借出"
✅ **借用记录**创建成功，关联到真实的员工ID

## 注意事项

1. **员工数据依赖**：确保数据库中有在职员工数据（`status = 'active'`）
2. **权限问题**：员工API需要认证，确保已登录
3. **空列表处理**：如果没有可用员工，显示友好提示
4. **错误处理**：如果加载失败，显示错误消息并允许重试

## 相关文件修改

| 文件 | 修改内容 |
|------|---------|
| `BorrowDialog.vue` | ✅ 添加员工列表加载逻辑 |
| `BorrowDialog.vue` | ✅ 添加加载状态显示 |
| `BorrowDialog.vue` | ✅ 添加空列表提示 |
| `BorrowDialog.vue` | ✅ 导入员工API和类型 |

---

**修复时间**: 2026-01-17
**状态**: ✅ 已完成
