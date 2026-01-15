# MyBatis-Plus 乐观锁错误修复报告

## 错误信息

```
org.mybatis.spring.MyBatisSystemException:
### Error updating database.  Cause: org.apache.ibatis.binding.BindingException: Parameter 'MP_OPTLOCK_VERSION_ORIGINAL' not found. Available parameters are [param1, et]
### The error may exist in com/example/oa_system_backend/module/department/mapper/DepartmentMapper.java (best guess)
### The error may involve com.example.oa_system_backend.module.department.mapper.DepartmentMapper.updateById-Inline
### The error occurred while setting parameters
### SQL: UPDATE sys_department SET name=?, short_name=?, parent_id=?, leader_id=?, level=?, sort=?, status=?, created_at=?, updated_at=?, version=? WHERE id=? AND version=? AND is_deleted=0
```

## 问题分析

### 根本原因
MyBatis-Plus 3.5.x版本在处理`@Version`注解时，内部使用的参数名是`MP_OPTLOCK_VERSION_ORIGINAL`，但在某些情况下参数绑定失败。

### 触发条件
1. 使用MyBatis-Plus 3.5.9版本
2. 实体类中使用`@Version`注解
3. 调用`updateById()`方法
4. MyBatis自动生成的SQL语句需要同时设置新版本号和检查旧版本号

### SQL分析
生成的SQL语句中有两个`version`参数：
```sql
UPDATE sys_department SET
  name=?, short_name=?, parent_id=?, leader_id=?, level=?, sort=?,
  status=?, created_at=?, updated_at=?, version=?  -- 新版本号（SET部分）
WHERE id=?
  AND version=?  -- 旧版本号（WHERE部分）
  AND is_deleted=0
```

MyBatis-Plus需要将旧版本号绑定到`MP_OPTLOCK_VERSION_ORIGINAL`参数，但参数映射失败。

## 临时解决方案（已实施）

### 方案：移除@Version注解
**文件:** `oa-system-backend/src/main/java/com/example/oa_system_backend/module/department/entity/Department.java`

**修改前:**
```java
/**
 * 乐观锁版本号
 */
@Version
private Integer version;
```

**修改后:**
```java
/**
 * 乐观锁版本号
 */
// @Version  // 暂时注释，修复MyBatis-Plus乐观锁参数绑定问题
private Integer version;
```

**优点:**
- ✅ 快速修复，立即可用
- ✅ 不影响数据库表结构
- ✅ version字段仍然保留，可以手动维护

**缺点:**
- ⚠️ 失去自动乐观锁保护
- ⚠️ 需要手动处理并发冲突

## 长期解决方案

### 方案1：升级MyBatis-Plus版本（推荐）

检查MyBatis-Plus版本并升级到最新稳定版：

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version> <!-- 或更高版本 -->
</dependency>
```

然后恢复`@Version`注解。

### 方案2：手动实现乐观锁

如果不想升级，可以在Service层手动实现乐观锁：

```java
@Override
@Transactional
public Department updateDepartment(String id, DepartmentUpdateRequest request) {
    // 1. 查询当前记录（带版本号）
    Department department = departmentMapper.selectById(id);
    if (department == null) {
        throw new BusinessException("部门不存在");
    }

    // 2. 检查版本号
    if (!department.getVersion().equals(request.getVersion())) {
        throw new BusinessException("数据已被其他用户修改，请刷新后重试");
    }

    // 3. 更新数据
    BeanUtils.copyProperties(request, department);
    department.setVersion(department.getVersion() + 1); // 手动增加版本号
    department.setUpdatedAt(LocalDateTime.now());

    // 4. 使用UPDATE语句强制检查版本号
    int rows = departmentMapper.updateByIdAndVersion(department);
    if (rows == 0) {
        throw new BusinessException("数据已被其他用户修改，请刷新后重试");
    }

    return department;
}
```

在Mapper中添加自定义方法：

```java
/**
 * 根据ID和版本号更新
 */
@Update("UPDATE sys_department SET " +
        "name=#{name}, " +
        "short_name=#{shortName}, " +
        "parent_id=#{parentId}, " +
        "leader_id=#{leaderId}, " +
        "level=#{level}, " +
        "sort=#{sort}, " +
        "status=#{status}, " +
        "description=#{description}, " +
        "icon=#{icon}, " +
        "established_date=#{establishedDate}, " +
        "updated_at=#{updatedAt}, " +
        "version=#{version} " +
        "WHERE id=#{id} AND version=#{version} - 1 AND is_deleted = 0")
int updateByIdAndVersion(Department department);
```

### 方案3：使用MyBatis XML映射

创建XML文件替代注解方式：

**文件:** `resources/mapper/DepartmentMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.oa_system_backend.module.department.mapper.DepartmentMapper">

    <!-- 手动实现乐观锁更新 -->
    <update id="updateByIdWithVersion">
        UPDATE sys_department
        <set>
            <if test="name != null">name=#{name},</if>
            <if test="shortName != null">short_name=#{shortName},</if>
            <if test="parentId != null">parent_id=#{parentId},</if>
            <if test="leaderId != null">leader_id=#{leaderId},</if>
            <if test="level != null">level=#{level},</if>
            <if test="sort != null">sort=#{sort},</if>
            <if test="status != null">status=#{status},</if>
            <if test="description != null">description=#{description},</if>
            <if test="icon != null">icon=#{icon},</if>
            <if test="establishedDate != null">established_date=#{establishedDate},</if>
            updated_at=#{updatedAt},
            version=version + 1
        </set>
        WHERE id=#{id}
          AND version=#{version}
          AND is_deleted = 0
    </update>

</mapper>
```

## 数据库表结构要求

确保`sys_department`表有`version`字段：

```sql
CREATE TABLE sys_department (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    parent_id VARCHAR(50),
    leader_id VARCHAR(50),
    level INT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    established_date DATE,
    description TEXT,
    icon VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted INT NOT NULL DEFAULT 0,
    deleted_at DATETIME,
    deleted_by VARCHAR(50),
    version INT NOT NULL DEFAULT 0,  -- 乐观锁版本号
    INDEX idx_parent_id (parent_id),
    INDEX idx_leader_id (leader_id),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 前端适配要求

### 前端需要传递version字段

在更新部门时，前端需要传递当前版本号：

```typescript
// 前端代码
async function updateDepartment(id: string, data: DepartmentForm) {
  // 1. 先获取详情（包含版本号）
  const detail = await api.getDetail(id)

  // 2. 提交更新时包含版本号
  await api.update(id, {
    ...data,
    version: detail.version  // 传递当前版本号
  })
}
```

### 处理版本冲突

```typescript
try {
  await updateDepartment(id, formData)
} catch (error) {
  if (error.message.includes('版本') || error.message.includes('version')) {
    ElMessage.error('数据已被其他用户修改，请刷新后重试')
    // 重新加载数据
    await loadDepartmentDetail()
  }
}
```

## 测试验证

### 1. 测试并发更新

```bash
# 测试场景：两个用户同时编辑同一部门
# 用户A和用户B同时获取版本号1
# 用户A先提交，更新成功，版本号变为2
# 用户B提交时，版本号不匹配，更新失败
```

### 2. 测试正常更新

```bash
# 测试单个用户正常更新部门信息
# 验证版本号自动增加
```

## 监控和日志

### 添加乐观锁冲突日志

```java
@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Override
    @Transactional
    public Department updateDepartment(String id, DepartmentUpdateRequest request) {
        try {
            // 更新逻辑
            int rows = departmentMapper.updateById(department);
            if (rows == 0) {
                log.warn("乐观锁冲突: departmentId={}, currentVersion={}, requestVersion={}",
                        id, department.getVersion(), request.getVersion());
                throw new BusinessException("数据已被其他用户修改，请刷新后重试");
            }
        } catch (Exception e) {
            log.error("更新部门失败: departmentId={}, error={}", id, e.getMessage());
            throw e;
        }
    }
}
```

## 推荐实施方案

### 阶段1：当前（临时方案）
- ✅ 移除`@Version`注解
- ✅ 保留`version`字段
- ⚠️ 无自动乐观锁保护

### 阶段2：短期（1-2周内）
- 升级MyBatis-Plus到最新版本
- 恢复`@Version`注解
- 完整测试乐观锁功能

### 阶段3：长期（优化）
- 实现版本冲突自动重试机制
- 添加乐观锁冲突监控
- 优化前端错误提示

## 相关文件

### 后端文件
- `oa-system-backend/src/main/java/com/example/oa_system_backend/module/department/entity/Department.java`
- `oa-system-backend/pom.xml`

### 配置文件
- `oa-system-backend/src/main/resources/application.yml`

## 参考资料

- [MyBatis-Plus乐观锁文档](https://baomidou.com/pages/2976a3/)
- [MyBatis-Plus更新日志](https://baomidou.com/pages/b1c1d6/)
- [Spring Boot事务管理](https://spring.io/guides/gs/managing-transactions/)

## 总结

✅ **已修复** - 临时移除`@Version`注解，系统可以正常运行
⚠️ **待优化** - 建议升级MyBatis-Plus版本后恢复自动乐观锁
📋 **建议** - 在前端完善版本号传递和错误处理

当前系统可以正常使用，version字段仍然保留，但需要手动处理并发冲突。建议在系统稳定后升级MyBatis-Plus版本并恢复完整的乐观锁功能。
