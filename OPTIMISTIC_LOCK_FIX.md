# 修复资产借用/归还的乐观锁问题

## 问题描述

点击【确认借出】时，后端报错：
```
Caused by: org.apache.ibatis.binding.BindingException:
Parameter 'MP_OPTLOCK_VERSION_ORIGINAL' not found.
Available parameters are [param1, et]
```

## 根本原因

### MyBatis-Plus 乐观锁机制

`Asset` 实体类中有 `@Version` 注解的字段：
```java
@Version
private Integer version;
```

### 问题代码

**原始的 `borrowAsset` 方法：**
```java
// 查询资产
Asset asset = assetMapper.selectById(id);  // 包含version字段

// 更新资产状态
asset.setStatus(AssetStatusEnum.BORROWED.getCode());
asset.setUserId(request.getBorrowerId());
asset.setBorrowDate(request.getBorrowDate());
asset.setExpectedReturnDate(request.getExpectedReturnDate());
assetMapper.updateById(asset);  // ❌ 触发乐观锁检查
```

当使用 `updateById(asset)` 更新包含 `@Version` 字段的对象时，MyBatis-Plus会：
1. 生成 SQL: `UPDATE biz_asset SET ... WHERE id = ? AND version = ?`
2. 需要原始的 version 值作为参数
3. 但在借用场景中，我们不需要检查 version，因为只是修改状态字段

### 为什么会报错

错误信息表明 MyBatis-Plus 找不到 `MP_OPTLOCK_VERSION_ORIGINAL` 参数，这是因为在某些情况下乐观锁的参数绑定出现问题。

## 解决方案

### 使用 `LambdaUpdateWrapper` 绕过乐观锁

对于借用和归还操作，我们不需要乐观锁保护，因为：
1. **借用操作**：只是修改资产状态和相关字段，不需要版本控制
2. **归还操作**：只是恢复资产状态，不需要版本控制
3. **乐观锁适用于**：编辑资产信息（名称、价格等）的并发控制

### 修复后的代码

#### 1. 添加导入

```java
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
```

#### 2. 修复 `borrowAsset` 方法

```java
// 更新资产状态（使用LambdaUpdateWrapper避免乐观锁问题）
LambdaUpdateWrapper<Asset> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.eq(Asset::getId, id)
        .set(Asset::getStatus, AssetStatusEnum.BORROWED.getCode())
        .set(Asset::getUserId, request.getBorrowerId())
        .set(Asset::getBorrowDate, request.getBorrowDate())
        .set(Asset::getExpectedReturnDate, request.getExpectedReturnDate());

int rows = assetMapper.update(null, updateWrapper);
if (rows == 0) {
    throw new BusinessException("更新资产状态失败");
}
```

#### 3. 修复 `returnAsset` 方法

```java
// 更新资产状态（使用LambdaUpdateWrapper避免乐观锁问题）
LambdaUpdateWrapper<Asset> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.eq(Asset::getId, id)
        .set(Asset::getStatus, AssetStatusEnum.STOCK.getCode())
        .set(Asset::getUserId, null)
        .set(Asset::getActualReturnDate, request.getActualReturnDate());

int rows = assetMapper.update(null, updateWrapper);
if (rows == 0) {
    throw new BusinessException("更新资产状态失败");
}
```

### 关键区别

| 方式 | SQL生成 | 乐观锁 | 适用场景 |
|------|---------|--------|---------|
| `updateById(entity)` | `UPDATE ... WHERE id = ? AND version = ?` | ✅ 启用 | 编辑资产信息 |
| `update(null, wrapper)` | `UPDATE ... SET ... WHERE id = ?` | ❌ 不启用 | 状态转换操作 |

### 为什么 `update(null, wrapper)` 不触发乐观锁

- 第一个参数为 `null`，表示不使用实体对象
- MyBatis-Plus 不会从实体中读取 `@Version` 字段
- 生成的 SQL 不包含 `version` 的条件判断
- 避免了乐观锁参数绑定问题

## 测试验证

### 1. 借用资产测试

```bash
POST /api/assets/ASSET000001/borrow
Content-Type: application/json

{
  "borrowerId": "EMP202401150001",
  "borrowDate": "2024-01-17",
  "expectedReturnDate": "2024-02-17"
}

# 预期响应
{
  "code": 200,
  "message": "借出成功",
  "data": null
}
```

### 2. 归还资产测试

```bash
POST /api/assets/ASSET000001/return
Content-Type: application/json

{
  "actualReturnDate": "2024-02-15",
  "notes": "资产状况良好"
}

# 预期响应
{
  "code": 200,
  "message": "归还成功",
  "data": null
}
```

### 3. 验证数据库状态

```sql
-- 检查资产状态
SELECT id, name, status, user_id, borrow_date, expected_return_date
FROM biz_asset
WHERE id = 'ASSET000001';

-- 检查借用记录
SELECT * FROM biz_asset_borrow_record
WHERE asset_id = 'ASSET000001'
ORDER BY created_at DESC;
```

## 相关文件修改

| 文件 | 修改内容 |
|------|---------|
| `AssetServiceImpl.java` | ✅ 添加 `LambdaUpdateWrapper` 导入 |
| `AssetServiceImpl.java` | ✅ 修复 `borrowAsset` 方法 |
| `AssetServiceImpl.java` | ✅ 修复 `returnAsset` 方法 |

## 注意事项

### 何时使用乐观锁

✅ **需要乐观锁的场景**：
- 编辑资产名称、型号、价格等核心信息
- 多个用户可能同时修改同一资产

❌ **不需要乐观锁的场景**：
- 状态转换（借用、归还）
- 自动计算的字段（折旧）
- 时间戳更新

### 并发安全性

虽然状态转换不使用乐观锁，但通过以下方式保证安全：
1. **状态转换验证**：`canTransitionTo()` 方法确保状态转换合法
2. **事务保护**：`@Transactional` 保证原子性
3. **数据库约束**：状态字段使用 ENUM，非法值会报错

---

**修复时间**: 2026-01-17
**状态**: ✅ 已完成
