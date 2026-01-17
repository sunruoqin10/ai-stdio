# 资产管理模块前后端联调指南

> **前端路径**: `oa-system-frontend/src/modules/asset`
> **后端路径**: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/asset`

---

## 1. 配置确认

### ✅ 前端配置（已完成）

**API代理配置** (`vite.config.ts`):
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  }
}
```

**HTTP请求配置** (`src/utils/request.ts`):
- baseURL: `/api`
- 自动添加JWT Token
- 统一错误处理

### ✅ 后端配置（已完成）

**CORS配置** (`SecurityConfig.java`):
```java
configuration.setAllowedOriginPatterns(List.of("*"));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(List.of("*"));
configuration.setAllowCredentials(true);
```

**安全配置** (`SecurityConfig.java`):
- 资产API需要认证: `/api/assets/**`
- 支持JWT Token认证

---

## 2. API端点映射

| 前端方法 | 后端端点 | HTTP方法 | 说明 |
|---------|---------|---------|------|
| `getAssets()` | `/api/assets` | GET | 分页查询资产列表 |
| `getAsset(id)` | `/api/assets/{id}` | GET | 查询资产详情 |
| `createAsset(data)` | `/api/assets` | POST | 创建资产 |
| `updateAsset(id, data)` | `/api/assets/{id}` | PUT | 更新资产 |
| `deleteAsset(id)` | `/api/assets/{id}` | DELETE | 删除资产 |
| `borrowAsset(id, data)` | `/api/assets/{id}/borrow` | POST | 借用资产 |
| `returnAsset(id, data)` | `/api/assets/{id}/return` | POST | 归还资产 |
| `getBorrowHistory(id)` | `/api/assets/{id}/borrow-history` | GET | 获取借用历史 |
| `getStatistics()` | `/api/assets/statistics` | GET | 获取资产统计 |

---

## 3. 数据格式映射

### 请求格式映射

#### 创建/更新资产
```typescript
// 前端请求 (AssetForm)
{
  name: string                  // 资产名称
  category: string              // electronic | furniture | book | other
  brandModel?: string           // 品牌/型号
  purchaseDate: string          // ISO 8601 日期
  purchasePrice: number         // 购置金额
  userId?: string               // 使用人ID
  location?: string             // 存放位置
  images?: string[]             // 图片URL数组
  notes?: string                // 备注
  version?: number              // 乐观锁版本号（更新时）
}

// 后端接收 (AssetCreateRequest/AssetUpdateRequest)
{
  name, category, brandModel, purchaseDate, purchasePrice,
  userId, location, images, notes, version
}
```

#### 借用资产
```typescript
// 前端请求 (BorrowForm)
{
  borrowerId: string            // 借用人ID
  borrowDate: string            // 借出日期 (ISO 8601)
  expectedReturnDate: string    // 预计归还日期 (ISO 8601)
  notes?: string                // 备注
}

// 后端接收 (AssetBorrowRequest)
{ borrowerId, borrowDate, expectedReturnDate, notes }
```

#### 归还资产
```typescript
// 前端请求 (ReturnForm)
{
  actualReturnDate: string      // 实际归还日期 (ISO 8601)
  notes?: string                // 备注
}

// 后端接收 (AssetReturnRequest)
{ actualReturnDate, notes }
```

### 响应格式映射

#### 资产对象
```typescript
// 后端响应 (AssetResponse)
{
  id: string
  name: string
  category: string
  categoryName: string          // 枚举描述
  brandModel: string
  purchaseDate: string          // LocalDate (YYYY-MM-DD)
  purchasePrice: number         // BigDecimal
  currentValue: number
  depreciationAmount: number     // 计算得出
  depreciationRate: number       // 百分比
  status: string
  statusName: string            // 枚举描述
  userId: string
  userName: string              // 关联查询
  userAvatar: string
  departmentId: string
  departmentName: string        // 关联查询
  location: string
  borrowDate: string
  expectedReturnDate: string
  actualReturnDate: string
  images: string[]              // JSON数组
  notes: string
  createdAt: string             // LocalDateTime
  updatedAt: string
  isOverdue: boolean            // 计算得出
  overdueDays: number
  ownedYears: number            // 计算得出
}
```

#### 统计数据
```typescript
// 后端响应 (AssetStatisticsResponse)
{
  totalCount: number
  totalPurchaseValue: number
  totalCurrentValue: number
  totalDepreciationAmount: number
  categoryStatistics: [
    {
      category: string
      categoryName: string
      count: number
      purchaseValue: number
      currentValue: number
    }
  ]
  statusStatistics: [
    {
      status: string
      statusName: string
      count: number
      value: number
    }
  ]
}
```

---

## 4. 启动测试步骤

### 步骤1: 启动后端服务

```bash
cd oa-system-backend
mvn spring-boot:run
```

**确认后端启动成功**:
- 服务地址: `http://localhost:8080`
- 查看日志确认资产模块已加载

### 步骤2: 启动前端服务

```bash
cd oa-system-frontend
npm run dev
```

**确认前端启动成功**:
- 服务地址: `http://localhost:3000`
- 浏览器自动打开

### 步骤3: 登录系统

1. 访问 `http://localhost:3000/login`
2. 输入用户名和密码
3. 获取JWT Token（自动存储到localStorage）

### 步骤4: 测试资产管理API

#### 测试1: 查询资产列表
```bash
# 使用浏览器开发者工具 -> Network
GET http://localhost:3000/api/assets?pageNum=1&pageSize=10

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 5,
    "current": 1,
    "size": 10
  }
}
```

#### 测试2: 创建资产
```bash
POST http://localhost:3000/api/assets
Content-Type: application/json

{
  "name": "测试资产",
  "category": "electronic",
  "purchaseDate": "2024-01-15",
  "purchasePrice": 10000
}

# 预期响应
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "ASSET000006",
    ...
  }
}
```

#### 测试3: 查询资产统计
```bash
GET http://localhost:3000/api/assets/statistics

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 6,
    "totalPurchaseValue": xxxxx,
    "totalCurrentValue": xxxxx,
    ...
  }
}
```

---

## 5. 常见问题排查

### 问题1: CORS错误
```
Access to XMLHttpRequest at 'http://localhost:8080/api/assets'
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**解决方案**:
- 确认后端SecurityConfig中CORS配置正确
- 确认前端vite.config.ts中proxy配置正确
- 重启后端服务

### 问题2: 401未授权
```
{"code":401,"message":"未授权"}
```

**解决方案**:
- 确认已登录并获取Token
- 检查浏览器localStorage中的token
- 确认请求头中包含 `Authorization: Bearer <token>`

### 问题3: 数据格式不匹配
```
{"code":400,"message":"购置日期不能是未来日期"}
```

**解决方案**:
- 检查前端发送的日期格式 (ISO 8601)
- 确认数值字段类型正确
- 检查必填字段是否完整

### 问题4: 乐观锁冲突
```
{"code":400,"message":"数据已被修改,请刷新后重试"}
```

**解决方案**:
- 刷新页面重新获取数据
- 确保version字段正确传递

---

## 6. 调试技巧

### 前端调试
1. 打开浏览器开发者工具 (F12)
2. Network标签查看所有HTTP请求
3. Console标签查看错误日志
4. Application标签查看localStorage中的Token

### 后端调试
1. 查看控制台日志
2. 使用Postman测试API
3. 检查数据库数据:
   ```sql
   SELECT * FROM biz_asset WHERE is_deleted = 0;
   SELECT * FROM biz_asset_borrow_record;
   ```

---

## 7. 性能优化建议

### 前端优化
- 使用分页加载，避免一次加载过多数据
- 图片使用懒加载
- 缓存统计数据，避免频繁请求

### 后端优化
- 添加数据库索引
- 使用Redis缓存统计数据
- 批量操作使用事务

---

## 8. 下一步开发

### 待完成功能
1. **折旧趋势API** (`/api/assets/depreciation-trend`)
2. **借用趋势API** (`/api/assets/borrow-trend`)
3. **资产批量导入/导出**
4. **资产二维码生成**
5. **资产维护记录管理**

### 增强功能
1. 添加WebSocket实时通知（借用即将到期）
2. 添加资产图片上传功能
3. 添加资产报表导出（Excel/PDF）
4. 添加资产审批流程

---

**文档版本**: v1.0.0
**创建日期**: 2026-01-17
**状态**: ✅ 可以开始联调测试
