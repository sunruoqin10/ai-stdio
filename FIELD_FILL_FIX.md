# 修复借用记录创建时间字段为空的问题

## 问题描述

点击【确认借出】时，后端报错：
```
Caused by: java.sql.SQLIntegrityConstraintViolationException:
Column 'created_at' cannot be null
```

## 根本原因

### MyBatis-Plus 字段自动填充未配置

`AssetBorrowRecord` 实体类中使用了 `@TableField(fill = FieldFill.INSERT)` 注解：
```java
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createdAt;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updatedAt;
```

但是项目中**没有配置 MyBatis-Plus 的 MetaObjectHandler**，导致这些字段在插入时没有被自动填充，数据库报错 `created_at cannot be null`。

### 为什么其他实体类没有问题

检查 `Employee` 实体类发现：
```java
@TableField("created_at")
private LocalDateTime createdAt;
```

它直接映射到数据库字段，不依赖自动填充，所以不需要 MetaObjectHandler。

## 解决方案

### 方案1：手动设置时间戳（推荐）

在插入借用记录时手动设置 `createdAt` 和 `updatedAt`：

```java
// 创建借用记录
AssetBorrowRecord record = new AssetBorrowRecord();
record.setAssetId(id);
record.setAssetName(asset.getName());
record.setBorrowerId(request.getBorrowerId());
record.setBorrowerName(borrower.getName());
record.setBorrowDate(request.getBorrowDate());
record.setExpectedReturnDate(request.getExpectedReturnDate());
record.setStatus(BorrowStatusEnum.ACTIVE.getCode());
record.setNotes(request.getNotes());
record.setCreatedAt(LocalDateTime.now()); // ✅ 手动设置创建时间
record.setUpdatedAt(LocalDateTime.now()); // ✅ 手动设置更新时间
assetBorrowRecordMapper.insert(record);
```

### 方案2：移除 FieldFill 注解（不推荐）

如果不想使用自动填充，可以修改实体类：
```java
// 移除 fill 参数
@TableField("created_at")
private LocalDateTime createdAt;

@TableField("updated_at")
private LocalDateTime updatedAt;
```

但这样需要在所有地方手动设置这两个字段。

### 方案3：配置 MetaObjectHandler（完整解决方案）

创建全局的元对象处理器：

```java
package com.example.oa_system_backend.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

然后在配置类中注册：
```java
@Configuration
public class MyBatisPlusConfig {
    // ...
}
```

## 修改内容

### 1. 添加导入

```java
import java.time.LocalDateTime;
```

### 2. 在 `borrowAsset` 方法中手动设置时间

```java
record.setCreatedAt(LocalDateTime.now());
record.setUpdatedAt(LocalDateTime.now());
```

## 测试验证

### 借用资产测试

```bash
POST /api/assets/ASSET000001/borrow
Content-Type: application/json

{
  "borrowerId": "EMP202401150001",
  "borrowDate": "2024-01-17",
  "expectedReturnDate": "2024-02-17"
}
```

### 验证数据库

```sql
-- 检查借用记录是否创建成功
SELECT * FROM biz_asset_borrow_record
ORDER BY created_at DESC
LIMIT 1;

-- 验证时间戳字段
SELECT
    id,
    asset_id,
    borrower_id,
    created_at,
    updated_at
FROM biz_asset_borrow_record
WHERE asset_id = 'ASSET000001';
```

## 相关文件修改

| 文件 | 修改内容 |
|------|---------|
| `AssetServiceImpl.java` | ✅ 添加 `LocalDateTime` 导入 |
| `AssetServiceImpl.java` | ✅ 在 `borrowAsset` 方法中手动设置 `createdAt` 和 `updatedAt` |

## 建议

为了保持代码一致性，建议：

1. **统一审计字段处理方式**：
   - 选项A：所有实体类都手动设置时间戳（当前方案）
   - 选项B：创建 `MyMetaObjectHandler` 全局处理器

2. **考虑创建 MetaObjectHandler**：
   - 避免在每个插入/更新操作中手动设置时间
   - 保持代码一致性
   - 更符合 MyBatis-Plus 的最佳实践

3. **数据库层面保护**：
   - 为 `created_at` 和 `updated_at` 设置默认值 `CURRENT_TIMESTAMP`
   - 作为最后的保护措施

---

**修复时间**: 2026-01-17
**状态**: ✅ 已完成
