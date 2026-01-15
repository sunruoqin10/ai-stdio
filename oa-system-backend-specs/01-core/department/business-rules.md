# 部门管理业务规则与约束

> **模块**: department
> **版本**: v1.0.0
> **更新日期**: 2026-01-15

---

## 🎯 业务规则概览

### 1. 部门层级规则

#### 1.1 层级限制

**规则**: 部门层级最多5级

**实现方式**:

```java
/**
 * 验证部门层级不超过5级
 */
public void validateLevelLimit(Integer level) {
    if (level > 5) {
        throw new BusinessException(2003, "部门层级不能超过5级");
    }
}
```

**触发场景**:
- 创建部门时
- 移动部门时

---

#### 1.2 层级计算规则

**规则**: 部门层级 = 父部门层级 + 1

**实现逻辑**:

```java
/**
 * 计算部门层级
 */
private Integer calculateLevel(String parentId) {
    if (parentId == null) {
        return 1; // 根级部门
    }

    Department parent = departmentMapper.selectById(parentId);
    if (parent == null) {
        throw new BusinessException(2009, "上级部门不存在");
    }

    return parent.getLevel() + 1;
}
```

**触发场景**:
- 创建部门时
- 移动部门时(递归更新所有子部门)

---

### 2. 部门名称规则

#### 2.1 名称唯一性约束

**规则**: 同一父部门下,部门名称必须唯一

**数据库约束**:

```sql
CREATE UNIQUE INDEX uk_department_name
ON sys_department(name, parent_id, is_deleted);
```

**业务验证**:

```java
/**
 * 验证部门名称唯一性
 */
public void validateNameUnique(String name, String parentId) {
    LambdaQueryWrapper<Department> wrapper = Wrappers.<Department>lambdaQuery()
            .eq(Department::getName, name)
            .eq(Department::getParentId, parentId == null ? "" : parentId)
            .eq(Department::getIsDeleted, 0);

    Long count = departmentMapper.selectCount(wrapper);
    if (count > 0) {
        throw new BusinessException(2002, "同级下已存在相同名称的部门");
    }
}
```

**触发场景**:
- 创建部门时
- 更新部门名称时

---

#### 2.2 名称长度约束

**规则**:
- 部门名称: 2-50字符
- 部门简称: 2-20字符

**实现方式**: 使用JSR-303验证注解

```java
@NotBlank(message = "部门名称不能为空")
@Size(min = 2, max = 50, message = "部门名称长度在2-50个字符之间")
private String name;

@Size(min = 2, max = 20, message = "部门简称长度在2-20个字符之间")
private String shortName;
```

---

### 3. 部门关系规则

#### 3.1 不能选择自己作为父部门

**规则**: 部门的parentId不能指向自己

**验证逻辑**:

```java
/**
 * 验证不能选择自己作为父部门
 */
public void validateNotSelfParent(String departmentId, String parentId) {
    if (departmentId.equals(parentId)) {
        throw new BusinessException(2004, "不能将部门设置为自己的父部门");
    }
}
```

**触发场景**:
- 更新部门父部门时
- 移动部门时

---

#### 3.2 不能选择子部门作为父部门

**规则**: 部门的parentId不能指向自己的任何子孙部门

**验证逻辑**:

```java
/**
 * 验证不能选择子部门作为父部门
 */
public void validateNotMoveToChild(String departmentId, String newParentId) {
    // 递归查询所有子孙部门ID
    List<String> descendantIds = treeService.getAllDescendantIds(departmentId);

    // 检查新父部门是否在子孙部门中
    if (descendantIds.contains(newParentId)) {
        throw new BusinessException(2005, "不能将部门移动到自己的子部门下");
    }
}
```

**实现**: 使用递归CTE查询

```sql
-- 递归查询所有子孙部门
WITH RECURSIVE dept_tree AS (
    -- 当前部门
    SELECT id, parent_id
    FROM sys_department
    WHERE id = #{departmentId} AND is_deleted = 0

    UNION ALL

    -- 递归子部门
    SELECT d.id, d.parent_id
    FROM sys_department d
    INNER JOIN dept_tree dt ON d.parent_id = dt.id
    WHERE d.is_deleted = 0
)
SELECT id FROM dept_tree WHERE id != #{departmentId};
```

**触发场景**:
- 移动部门时

---

### 4. 部门删除规则

#### 4.1 有子部门的部门不能删除

**规则**: 删除部门前,必须确保没有子部门

**验证逻辑**:

```java
/**
 * 验证没有子部门
 */
public void validateNoChildren(String departmentId) {
    Long childCount = departmentMapper.selectCount(
            Wrappers.<Department>lambdaQuery()
                    .eq(Department::getParentId, departmentId)
                    .eq(Department::getIsDeleted, 0)
    );

    if (childCount > 0) {
        throw new BusinessException(2006, "该部门下还有子部门,请先删除或移动子部门");
    }
}
```

**数据库触发器** (可选,双重保障):

```sql
DELIMITER $$

CREATE TRIGGER trg_department_before_delete
BEFORE UPDATE ON sys_department
FOR EACH ROW
BEGIN
  -- 检查是否标记为删除
  IF NEW.is_deleted = 1 AND OLD.is_deleted = 0 THEN
    -- 检查是否有子部门
    DECLARE child_count INT;
    SELECT COUNT(*) INTO child_count
    FROM sys_department
    WHERE parent_id = NEW.id AND is_deleted = 0;

    IF child_count > 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '该部门下还有子部门,无法删除';
    END IF;
  END IF;
END$$

DELIMITER ;
```

---

#### 4.2 有员工的部门不能删除

**规则**: 删除部门前,必须确保部门内没有员工

**验证逻辑**:

```java
/**
 * 验证没有员工
 */
public void validateNoEmployees(String departmentId) {
    // 调用员工服务查询该部门的员工数量
    Long employeeCount = employeeService.countByDepartmentId(departmentId);

    if (employeeCount > 0) {
        throw new BusinessException(2007,
                String.format("该部门下还有 %d 名员工,请先转移或删除员工", employeeCount));
    }
}
```

**SQL查询**:

```sql
SELECT COUNT(*)
FROM sys_employee
WHERE department_id = #{departmentId}
  AND is_deleted = 0;
```

**触发场景**:
- 删除部门时
- 批量删除部门时

---

### 5. 部门负责人规则

#### 5.1 负责人必须存在

**规则**: 部门的leaderId必须指向一个存在的员工

**验证逻辑**:

```java
/**
 * 验证负责人存在
 */
public void validateLeaderExists(String leaderId) {
    // 调用员工服务验证员工是否存在
    boolean exists = employeeService.existsById(leaderId);

    if (!exists) {
        throw new BusinessException(2010, "指定的负责人不存在");
    }
}
```

**数据库外键约束**:

```sql
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_leader
  FOREIGN KEY (leader_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

**触发场景**:
- 创建部门时
- 更新部门负责人时

---

#### 5.2 负责人离职处理

**规则**: 当部门负责人离职时,需要指定新的负责人或清空负责人

**处理方式**:

1. **方式一**: 禁止删除/离职有部门负责责任的员工
2. **方式二**: 自动清空部门的leaderId
3. **方式三**: 要求指定新的负责人

**推荐实现**: 方式三(业务流程控制)

```java
/**
 * 员工离职前检查
 */
public void validateEmployeeResign(String employeeId) {
    // 查询该员工负责的部门
    List<Department> departments = departmentMapper.selectList(
            Wrappers.<Department>lambdaQuery()
                    .eq(Department::getLeaderId, employeeId)
                    .eq(Department::getIsDeleted, 0)
    );

    if (!departments.isEmpty()) {
        String deptNames = departments.stream()
                .map(Department::getName)
                .collect(Collectors.joining("、"));

        throw new BusinessException(3001,
                String.format("该员工是以下部门的负责人: %s,请先指定新的负责人", deptNames));
    }
}
```

---

### 6. 部门移动规则

#### 6.1 移动后层级更新规则

**规则**: 移动部门时,需要递归更新所有子孙部门的层级

**实现逻辑**:

```java
/**
 * 移动部门树(更新层级)
 */
@Transactional(rollbackFor = Exception.class)
public void moveDepartmentTree(String departmentId, String newParentId) {
    // 1. 计算新层级
    Integer newLevel;
    if (newParentId == null) {
        newLevel = 1;
    } else {
        Department newParent = departmentMapper.selectById(newParentId);
        newLevel = newParent.getLevel() + 1;
    }

    // 2. 获取当前部门层级
    Department current = departmentMapper.selectById(departmentId);
    Integer currentLevel = current.getLevel();

    // 3. 计算层级偏移量
    Integer levelOffset = newLevel - currentLevel;

    // 4. 递归更新所有子孙部门的层级
    List<String> descendantIds = getAllDescendantIds(departmentId);

    // 更新当前部门
    current.setParentId(newParentId);
    current.setLevel(newLevel);
    departmentMapper.updateById(current);

    // 更新所有子孙部门
    for (String descendantId : descendantIds) {
        Department descendant = departmentMapper.selectById(descendantId);
        descendant.setLevel(descendant.getLevel() + levelOffset);
        departmentMapper.updateById(descendant);
    }
}
```

---

#### 6.2 移动验证规则

**规则**: 移动部门前需要验证:
1. 不能移动到自己
2. 不能移动到子部门
3. 新父部门必须存在
4. 移动后层级不能超过5级

**综合验证**:

```java
/**
 * 移动部门验证
 */
public void validateMove(String departmentId, String newParentId) {
    // 1. 验证部门存在
    Department department = departmentMapper.selectById(departmentId);
    if (department == null) {
        throw new BusinessException(2001, "部门不存在");
    }

    // 2. 验证新父部门存在(如果不为null)
    if (newParentId != null) {
        Department newParent = departmentMapper.selectById(newParentId);
        if (newParent == null) {
            throw new BusinessException(2009, "新父部门不存在");
        }

        // 3. 验证不能移动到自己
        if (departmentId.equals(newParentId)) {
            throw new BusinessException(2004, "不能将部门设置为自己的父部门");
        }

        // 4. 验证不能移动到子部门
        validateNotMoveToChild(departmentId, newParentId);

        // 5. 验证移动后层级不超过5级
        Integer newLevel = newParent.getLevel() + 1;
        if (newLevel > 5) {
            throw new BusinessException(2003, "移动后层级不能超过5级");
        }
    }
}
```

---

### 7. 并发控制规则

#### 7.1 乐观锁控制

**规则**: 使用version字段实现乐观锁,防止并发冲突

**实现方式**: MyBatis-Plus @Version注解

```java
/**
 * 乐观锁版本号
 */
@Version
private Integer version;
```

**更新流程**:

```java
/**
 * 更新部门(带乐观锁)
 */
public void updateDepartmentWithVersion(String id, DepartmentUpdateDTO updateDTO) {
    // 1. 查询当前版本
    Department department = departmentMapper.selectById(id);
    if (!department.getVersion().equals(updateDTO.getVersion())) {
        throw new BusinessException(2008, "数据已被其他用户修改,请刷新后重试");
    }

    // 2. 更新数据
    department.setName(updateDTO.getName());
    department.setVersion(department.getVersion() + 1);

    // 3. 执行更新(MyBatis-Plus会自动处理version)
    int rows = departmentMapper.updateById(department);
    if (rows == 0) {
        throw new BusinessException(2008, "更新失败,数据已被其他用户修改");
    }
}
```

**SQL执行**:

```sql
UPDATE sys_department
SET name = #{name},
    version = version + 1
WHERE id = #{id}
  AND version = #{oldVersion};
```

---

### 8. 数据完整性规则

#### 8.1 外键约束

**约束1**: 父部门外键

```sql
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_parent
  FOREIGN KEY (parent_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

**约束说明**:
- `ON DELETE RESTRICT`: 有子部门的部门不能删除
- `ON UPDATE CASCADE`: 部门ID更新时,自动更新子部门的parentId

---

**约束2**: 负责人外键

```sql
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_leader
  FOREIGN KEY (leader_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

**约束说明**:
- `ON DELETE RESTRICT`: 有员工的部门不能删除(通过触发器实现)
- `ON UPDATE CASCADE`: 员工ID更新时,自动更新部门的leaderId

---

#### 8.2 检查约束

**约束1**: 层级范围约束

```sql
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_level
  CHECK (level BETWEEN 1 AND 5);
```

**约束2**: 状态枚举约束

```sql
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_status
  CHECK (status IN ('active', 'disabled'));
```

**约束3**: 排序号非负约束

```sql
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_sort
  CHECK (sort >= 0);
```

---

### 9. 软删除规则

#### 9.1 软删除实现

**规则**: 删除部门时,只标记is_deleted=1,不物理删除数据

**实现逻辑**:

```java
/**
 * 软删除部门
 */
public void softDelete(String id) {
    Department department = departmentMapper.selectById(id);

    department.setIsDeleted(1);
    department.setDeletedAt(LocalDateTime.now());
    department.setDeletedBy(CurrentUser.getId());

    departmentMapper.updateById(department);
}
```

**MyBatis-Plus配置**:

```java
/**
 * 软删除字段
 */
@TableLogic
private Integer isDeleted;
```

**SQL自动处理**:

```sql
-- 查询时自动添加 WHERE is_deleted = 0
SELECT * FROM sys_department WHERE id = #{id} AND is_deleted = 0;

-- 删除时自动执行 UPDATE
UPDATE sys_department SET is_deleted = 1 WHERE id = #{id};
```

---

#### 9.2 软删除级联规则

**规则**: 删除父部门时,需要级联标记所有子部门为已删除

**实现逻辑**:

```java
/**
 * 级联软删除部门及其子部门
 */
@Transactional(rollbackFor = Exception.class)
public void cascadeSoftDelete(String id) {
    // 1. 获取所有子孙部门ID
    List<String> descendantIds = getAllDescendantIds(id);

    // 2. 标记当前部门为已删除
    Department department = departmentMapper.selectById(id);
    department.setIsDeleted(1);
    department.setDeletedAt(LocalDateTime.now());
    departmentMapper.updateById(department);

    // 3. 级联标记所有子孙部门
    for (String descendantId : descendantIds) {
        Department descendant = departmentMapper.selectById(descendantId);
        descendant.setIsDeleted(1);
        descendant.setDeletedAt(LocalDateTime.now());
        departmentMapper.updateById(descendant);
    }
}
```

---

### 10. 审计规则

#### 10.1 创建审计

**规则**: 创建部门时,自动记录创建人和创建时间

**实现方式**: MyBatis-Plus @TableField注解 + MetaObjectHandler

```java
/**
 * 创建时间
 */
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createdAt;

/**
 * 创建人ID
 */
@TableField(fill = FieldFill.INSERT)
private String createdBy;
```

**MetaObjectHandler实现**:

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdBy", String.class, CurrentUser.getId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", String.class, CurrentUser.getId());
    }
}
```

---

#### 10.2 更新审计

**规则**: 更新部门时,自动记录更新人和更新时间

```java
/**
 * 更新时间
 */
@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updatedAt;

/**
 * 更新人ID
 */
@TableField(fill = FieldFill.INSERT_UPDATE)
private String updatedBy;
```

---

#### 10.3 删除审计

**规则**: 删除部门时,自动记录删除人和删除时间

```java
/**
 * 删除时间
 */
private LocalDateTime deletedAt;

/**
 * 删除人ID
 */
private String deletedBy;
```

**删除时设置**:

```java
public void softDelete(String id) {
    Department department = departmentMapper.selectById(id);

    department.setIsDeleted(1);
    department.setDeletedAt(LocalDateTime.now());
    department.setDeletedBy(CurrentUser.getId()); // 记录删除人

    departmentMapper.updateById(department);
}
```

---

## 📋 业务规则总结

| 规则类别 | 规则名称 | 触发场景 | 实现方式 |
|---------|---------|---------|---------|
| 层级规则 | 层级限制(最多5级) | 创建/移动 | 业务验证 + CHECK约束 |
| 层级规则 | 层级自动计算 | 创建/移动 | 业务逻辑 |
| 名称规则 | 同级名称唯一 | 创建/更新 | 业务验证 + UNIQUE索引 |
| 名称规则 | 名称长度限制 | 创建/更新 | JSR-303验证 |
| 关系规则 | 不能自关联 | 更新/移动 | 业务验证 |
| 关系规则 | 不能关联子部门 | 移动 | 业务验证 + 递归查询 |
| 删除规则 | 有子部门不能删除 | 删除 | 业务验证 + 外键约束 |
| 删除规则 | 有员工不能删除 | 删除 | 业务验证 |
| 负责人规则 | 负责人必须存在 | 创建/更新 | 业务验证 + 外键约束 |
| 负责人规则 | 负责人离职处理 | 员工离职 | 业务流程控制 |
| 移动规则 | 层级递归更新 | 移动 | 业务逻辑 + 事务 |
| 并发规则 | 乐观锁控制 | 更新 | @Version注解 |
| 完整性规则 | 外键约束 | 所有操作 | FOREIGN KEY |
| 完整性规则 | 检查约束 | 所有操作 | CHECK |
| 审计规则 | 创建审计 | 创建 | @TableField + Handler |
| 审计规则 | 更新审计 | 更新 | @TableField + Handler |
| 审计规则 | 删除审计 | 删除 | 手动设置 |

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-15
