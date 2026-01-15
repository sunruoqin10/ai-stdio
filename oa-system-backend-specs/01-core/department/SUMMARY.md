# 部门管理模块 - 后端规范总结

> **模块**: department
> **技术栈**: Spring Boot 3.x + MyBatis-Plus 3.5.x + MySQL 8.0+
> **版本**: v1.0.0
> **创建日期**: 2026-01-15

---

## 📚 文档结构

```
oa-system-backend-specs/01-core/department/
├── README.md                    # 模块概述和架构设计
├── api-specification.md        # Controller层API接口规范
├── service-specification.md    # Service层业务逻辑规范
├── business-rules.md           # 业务规则与约束
├── mapper-specification.md     # Mapper层数据访问规范
└── SUMMARY.md                  # 本文档(总结)
```

---

## 🎯 核心功能

### 1. 部门CRUD操作
- ✅ 创建部门
- ✅ 更新部门
- ✅ 删除部门(软删除)
- ✅ 查询部门(树形/扁平)

### 2. 树形结构管理
- ✅ 最多5级层级
- ✅ 自动层级计算
- ✅ 部门移动与调整
- ✅ 递归更新子部门层级

### 3. 关联管理
- ✅ 部门负责人设置
- ✅ 部门成员管理
- ✅ 父子关系维护

### 4. 查询与搜索
- ✅ 树形结构查询
- ✅ 扁平列表查询
- ✅ 关键词搜索
- ✅ 多条件筛选

### 5. 统计与导出
- ✅ 部门统计信息
- ✅ Excel导出功能
- ✅ 批量操作支持

---

## 🏗️ 技术架构

### 分层架构

```
Controller Layer (RESTful API)
    ↓
Service Layer (Business Logic)
    ↓
Mapper Layer (Data Access)
    ↓
Database Layer (MySQL 8.0+)
```

### 核心组件

| 组件 | 类名 | 职责 |
|------|------|------|
| Controller | DepartmentController | 提供RESTful API接口 |
| Service | DepartmentService | 核心业务逻辑 |
| Service | DepartmentQueryService | 查询服务 |
| Service | DepartmentValidateService | 验证服务 |
| Service | DepartmentTreeService | 树形结构服务 |
| Mapper | DepartmentMapper | 部门数据访问 |
| Mapper | DepartmentMemberMapper | 部门成员数据访问 |

---

## 🔒 业务规则

### 1. 层级规则
- **层级限制**: 最多5级
- **层级计算**: 子部门层级 = 父部门层级 + 1
- **移动验证**: 移动后不能超过5级

### 2. 命名规则
- **唯一性**: 同一父部门下,部门名称必须唯一
- **长度限制**:
  - 部门名称: 2-50字符
  - 部门简称: 2-20字符

### 3. 关系规则
- **不能自关联**: 部门不能选择自己作为父部门
- **不能循环关联**: 部门不能选择子部门作为父部门

### 4. 删除规则
- **无子部门**: 有子部门的部门不能删除
- **无员工**: 有员工的部门不能删除
- **软删除**: 采用逻辑删除,保留数据

### 5. 负责人规则
- **必须存在**: 负责人必须是有效的员工ID
- **外键约束**: 通过外键约束保证数据完整性

### 6. 并发控制
- **乐观锁**: 使用version字段实现乐观锁
- **版本验证**: 更新时验证版本号,防止并发冲突

---

## 📊 数据库设计

### 核心表

#### 1. sys_department (部门表)

```sql
CREATE TABLE sys_department (
  id VARCHAR(20) PRIMARY KEY COMMENT '部门编号(DEPT+4位序号)',
  name VARCHAR(100) NOT NULL COMMENT '部门名称',
  short_name VARCHAR(50) COMMENT '部门简称',
  parent_id VARCHAR(20) COMMENT '上级部门ID',
  leader_id VARCHAR(20) NOT NULL COMMENT '负责人ID',
  level INT NOT NULL DEFAULT 1 COMMENT '部门层级(1-5)',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
  established_date DATE COMMENT '成立日期',
  description TEXT COMMENT '部门描述',
  icon VARCHAR(500) COMMENT '部门图标URL',
  status ENUM('active', 'disabled') DEFAULT 'active' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门信息表';
```

#### 2. sys_department_member (部门成员关系表)

```sql
CREATE TABLE sys_department_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
  department_id VARCHAR(20) NOT NULL COMMENT '部门ID',
  employee_id VARCHAR(20) NOT NULL COMMENT '员工ID',
  is_leader TINYINT(1) DEFAULT 0 COMMENT '是否为负责人',
  join_date DATE NOT NULL COMMENT '加入部门日期',
  leave_date DATE COMMENT '离开部门日期',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门成员关系表';
```

### 索引设计

```sql
-- 部门表索引
CREATE UNIQUE INDEX uk_department_name ON sys_department(name, parent_id, is_deleted);
CREATE INDEX idx_department_parent ON sys_department(parent_id);
CREATE INDEX idx_department_leader ON sys_department(leader_id);
CREATE INDEX idx_department_level ON sys_department(level);
CREATE INDEX idx_department_status ON sys_department(status);
CREATE INDEX idx_department_sort ON sys_department(parent_id, sort);

-- 部门成员表索引
CREATE UNIQUE INDEX uk_dept_member ON sys_department_member(department_id, employee_id, leave_date);
CREATE INDEX idx_dept_member_emp ON sys_department_member(employee_id);
```

### 外键约束

```sql
-- 父部门外键
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_parent
  FOREIGN KEY (parent_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 负责人外键
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_leader
  FOREIGN KEY (leader_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

### 检查约束

```sql
-- 层级范围约束
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_level
  CHECK (level BETWEEN 1 AND 5);

-- 状态枚举约束
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_status
  CHECK (status IN ('active', 'disabled'));

-- 排序号非负约束
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_sort
  CHECK (sort >= 0);
```

---

## 🔌 API接口列表

### 查询接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/departments | 获取部门列表(树形/扁平) |
| GET | /api/departments/{id} | 获取部门详情 |
| GET | /api/departments/{id}/children | 获取子部门列表 |
| GET | /api/departments/{id}/employees | 获取部门成员列表 |
| GET | /api/departments/statistics | 获取部门统计信息 |

### 操作接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/departments | 创建部门 |
| PUT | /api/departments/{id} | 更新部门 |
| PUT | /api/departments/{id}/move | 移动部门 |
| DELETE | /api/departments/{id} | 删除部门 |
| DELETE | /api/departments/batch | 批量删除部门 |
| POST | /api/departments/export | 导出部门列表 |

---

## 🛡️ 数据验证

### 前端验证

```typescript
// 部门名称
{
  required: true,
  message: '请输入部门名称',
  trigger: 'blur'
},
{
  min: 2,
  max: 50,
  message: '长度在 2 到 50 个字符',
  trigger: 'blur'
}

// 部门简称
{
  min: 2,
  max: 20,
  message: '长度在 2 到 20 个字符',
  trigger: 'blur'
}

// 负责人
{
  required: true,
  message: '请选择部门负责人',
  trigger: 'change'
}

// 排序号
{
  type: 'number',
  min: 0,
  message: '排序号必须大于等于0',
  trigger: 'blur'
}
```

### 后端验证

```java
// JSR-303验证
@NotBlank(message = "部门名称不能为空")
@Size(min = 2, max = 50, message = "部门名称长度在2-50个字符之间")
private String name;

@Size(min = 2, max = 20, message = "部门简称长度在2-20个字符之间")
private String shortName;

@NotBlank(message = "部门负责人不能为空")
private String leaderId;

@Min(value = 0, message = "排序号不能小于0")
private Integer sort;
```

### 数据库约束

```sql
-- 唯一约束
CREATE UNIQUE INDEX uk_department_name
ON sys_department(name, parent_id, is_deleted);

-- 检查约束
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_level
  CHECK (level BETWEEN 1 AND 5);

-- 外键约束
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_leader
  FOREIGN KEY (leader_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

---

## 💾 缓存策略

### 缓存方案

使用 **Caffeine** 作为本地内存缓存,提供高性能的缓存方案。

**优势**:
- 无需额外的Redis服务
- 更低的延迟(内存直接访问)
- 自动过期策略
- 缓存统计支持
- 简化部署和运维

### 缓存配置

| 缓存名称 | 缓存内容 | TTL | 最大容量 | 失效策略 |
|---------|---------|-----|---------|---------|
| department:list | 部门列表 | 5分钟 | 500 | 增删改时清除 |
| department:detail | 部门详情 | 10分钟 | 500 | 增删改时清除 |
| department:children | 子部门列表 | 5分钟 | 500 | 增删改时清除 |
| department:members | 部门成员 | 5分钟 | 500 | 增删改时清除 |
| department:statistics | 统计信息 | 1分钟 | 100 | 定时刷新 |

### 缓存注解

```java
// 查询时使用缓存
@Cacheable(value = "department:detail", key = "#id")
public DepartmentDetailVO getDepartmentDetail(String id) {
    // ...
}

// 更新时清除缓存
@CacheEvict(value = {"department:list", "department:detail"}, allEntries = true)
public void updateDepartment(String id, DepartmentUpdateDTO updateDTO) {
    // ...
}
```

### 缓存配置示例

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.addCaches(buildCache("department:list", 5));
        // ...
        return cacheManager;
    }

    private CaffeineCache buildCache(String name, int ttlMinutes) {
        return new CaffeineCache(name,
            Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build()
        );
    }
}
```

---

## 🚀 性能优化

### 1. 查询优化

- **递归CTE**: 使用MySQL 8.0+的递归CTE查询树形结构
- **索引优化**: 为常用查询字段创建索引
- **分页查询**: 大数据量时使用分页
- **延迟加载**: 关联数据按需加载

### 2. 缓存优化

- **Caffeine缓存**: 使用Caffeine本地缓存热点数据
- **多层缓存**: 针对不同数据设置不同的TTL
- **缓存预热**: 系统启动时加载常用数据
- **缓存监控**: 通过recordStats()监控缓存命中率

### 3. 批量操作

- **批量插入**: 使用批量插入减少SQL执行次数
- **批量更新**: 使用CASE WHEN或临时表
- **事务管理**: 合理使用事务保证数据一致性

---

## 📋 开发检查清单

### 数据库

- [ ] 创建sys_department表
- [ ] 创建sys_department_member表
- [ ] 创建所有索引
- [ ] 创建外键约束
- [ ] 创建检查约束
- [ ] 初始化测试数据

### 后端代码

- [ ] 创建Department实体类
- [ ] 创建DepartmentMember实体类
- [ ] 创建DTO/VO类
- [ ] 创建DepartmentMapper接口
- [ ] 创建DepartmentMemberMapper接口
- [ ] 创建Mapper XML文件
- [ ] 创建DepartmentService接口
- [ ] 创建DepartmentServiceImpl实现类
- [ ] 创建DepartmentQueryService
- [ ] 创建DepartmentValidateService
- [ ] 创建DepartmentTreeService
- [ ] 创建DepartmentController
- [ ] 实现所有业务逻辑
- [ ] 实现所有验证规则
- [ ] 实现缓存策略

### 测试

- [ ] 单元测试(Service层)
- [ ] 集成测试(Mapper层)
- [ ] API测试(Controller层)
- [ ] 性能测试
- [ ] 并发测试

---

## 📖 相关文档

### 前端规范
- [部门管理前端规范](../../oa-system-frontend-specs/core/department/department_Technical.md)

### 数据库规范
- [部门管理数据库设计](../../oa-system-database-specs/01-core/department.md)

### API文档
- [Swagger API文档](http://localhost:8080/swagger-ui.html)

---

## 🔄 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.1 | 2026-01-15 | 使用Caffeine替代Redis作为缓存方案 |
| v1.0.0 | 2026-01-15 | 初始版本,完成核心功能设计 |

---

## 👥 开发团队

- **产品经理**: 待定
- **后端开发**: 待定
- **前端开发**: 待定
- **测试工程师**: 待定
- **UI设计师**: 待定

---

**文档版本**: v1.0.1
**创建人**: AI开发助手
**最后更新**: 2026-01-15
**更新内容**: 使用Caffeine替代Redis作为缓存方案
