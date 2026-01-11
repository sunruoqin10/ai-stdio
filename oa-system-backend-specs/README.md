# OA系统后端技术规范文档

> 本文档严格基于前端规格规范 `oa-system-frontend-specs\` 和数据库规格规范 `oa-system-database-specs\` 创建
> **技术栈**: Spring Boot 3.x + MyBatis-Plus + MySQL 8.0+
> **版本**: v1.0.0

---

## 📋 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术架构](#2-技术架构)
- [3. 项目结构](#3-项目结构)
- [4. 开发规范](#4-开发规范)
- [5. 数据库约束实现](#5-数据库约束实现)
- [6. 模块列表](#6-模块列表)

---

## 1. 项目概述

### 1.1 项目信息

- **项目名称**: OA系统后端
- **技术栈**: Spring Boot 3.2 + MyBatis-Plus 3.5 + MySQL 8.0
- **JDK版本**: JDK 17
- **构建工具**: Maven 3.9+
- **编码规范**: 遵循阿里巴巴Java开发手册

### 1.2 核心依赖

```xml
<!-- Spring Boot -->
<spring-boot.version>3.2.0</spring-boot.version>

<!-- MyBatis-Plus -->
<mybatis-plus.version>3.5.5</mybatis-plus.version>

<!-- 数据库连接池 -->
<druid.version>1.2.20</druid.version>

<!-- JWT -->
<jjwt.version>0.12.3</jjwt.version>

<!-- 工具类 -->
<hutool.version>5.8.23</hutool.version>

<!-- 文档 -->
<knife4j.version>4.3.0</knife4j.version>
<springdoc.version>2.2.0</springdoc.version>
```

---

## 2. 技术架构

### 2.1 分层架构

```
┌─────────────────────────────────────┐
│         Controller Layer            │  接口层
├─────────────────────────────────────┤
│          Service Layer              │  业务逻辑层
├─────────────────────────────────────┤
│          Mapper Layer               │  数据访问层
├─────────────────────────────────────┤
│          Database Layer             │  数据库层
└─────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 Controller层
- **职责**: 接收HTTP请求,参数验证,返回响应
- **注解**: `@RestController`, `@RequestMapping`, `@Validated`
- **规范**:
  - 统一使用RESTful风格
  - 统一返回格式 `Result<T>`
  - 必须进行参数校验

#### 2.2.2 Service层
- **职责**: 业务逻辑处理,事务管理
- **注解**: `@Service`, `@Transactional`
- **规范**:
  - 接口定义在 `service` 包
  - 实现在 `service.impl` 包
  - 所有业务方法必须添加事务注解

#### 2.2.3 Mapper层
- **职责**: 数据库CRUD操作
- **注解**: `@Mapper`
- **规范**:
  - 继承 `BaseMapper<T>`
  - 使用MyBatis-Plus注解或XML
  - 复杂查询使用XML

#### 2.2.4 Entity层
- **职责**: 数据库表实体映射
- **注解**: `@TableName`, `@TableId`, `@TableField`
- **规范**:
  - 使用Lombok简化代码
  - 必须添加数据库字段映射注解
  - 实现数据库约束逻辑

---

## 3. 项目结构

```
oa-system-backend/
├── src/main/java/com/oa/system/
│   ├── OaSystemApplication.java      # 启动类
│   │
│   ├── common/                       # 通用模块
│   │   ├── config/                   # 配置类
│   │   ├── constant/                 # 常量定义
│   │   ├── exception/                # 异常处理
│   │   ├── result/                   # 统一返回结果
│   │   ├── util/                     # 工具类
│   │   └── annotation/               # 自定义注解
│   │
│   ├── core/                         # 核心模块
│   │   ├── employee/                 # 员工管理
│   │   ├── department/               # 部门管理
│   │   ├── asset/                    # 资产管理
│   │   ├── dict/                     # 数据字典
│   │   └── menu/                     # 菜单管理
│   │
│   ├── approval/                     # 审批模块
│   │   ├── leave/                    # 请假审批
│   │   └── expense/                  # 费用报销
│   │
│   ├── admin/                        # 管理模块
│   │   └── meeting/                  # 会议室管理
│   │
│   ├── auth/                         # 认证授权
│   │   ├── controller/               # 认证控制器
│   │   ├── service/                  # 认证服务
│   │   ├── security/                 # 安全相关
│   │   └── filter/                   # 过滤器
│   │
│   └── integration/                  # 集成模块
│       ├── schedule/                 # 定时任务
│       └── listener/                 # 监听器
│
└── src/main/resources/
    ├── mapper/                       # MyBatis XML
    ├── application.yml               # 主配置文件
    ├── application-dev.yml           # 开发环境
    ├── application-test.yml          # 测试环境
    └── application-prod.yml          # 生产环境
```

---

## 4. 开发规范

### 4.1 命名规范

#### 4.1.1 包命名
- 全部小写,使用点分隔
- 格式: `com.oa.system.{模块}.{层}`

#### 4.1.2 类命名
- **Controller**: `{模块}Controller`
- **Service接口**: `{模块}Service`
- **Service实现**: `{模块}ServiceImpl`
- **Mapper**: `{模块}Mapper`
- **Entity**: `{表名}` (使用下划线转驼峰)

#### 4.1.3 方法命名
- **查询**: `get{Entity}`, `list{Entity}`, `page{Entity}`
- **新增**: `save{Entity}`, `insert{Entity}`
- **修改**: `update{Entity}`
- **删除**: `remove{Entity}`, `delete{Entity}`

### 4.2 注解规范

#### 4.2.1 Controller注解
```java
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "员工管理", description = "员工管理接口")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    @Operation(summary = "获取员工详情")
    public Result<EmployeeVO> getEmployee(
        @PathVariable String id
    ) {
        EmployeeVO employee = employeeService.getEmployeeById(id);
        return Result.success(employee);
    }
}
```

#### 4.2.2 Service注解
```java
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public EmployeeVO getEmployeeById(String id) {
        // 实现逻辑
    }
}
```

#### 4.2.3 Entity注解
```java
@TableName("sys_employee")
@Data
public class Employee {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("name")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @TableField("department_id")
    @NotNull(message = "部门不能为空")
    private String departmentId;

    // 审计字段
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @TableField("version")
    @Version
    private Integer version;
}
```

---

## 5. 数据库约束实现

### 5.1 实体层约束实现

根据数据库规格 `oa-system-database-specs\schemas\03_create_constraints.sql` 中的约束,在Entity层实现对应的Java Bean Validation。

#### 5.1.1 员工表约束

```java
@TableName("sys_employee")
@Data
public class Employee {

    @TableId(value = "id", type = IdType.INPUT)
    @Pattern(regexp = "^EMP\\d{15}$", message = "员工ID格式不正确")
    private String id;

    @TableField("name")
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符")
    private String name;

    @TableField("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @UniqueCheck(field = "email", message = "邮箱已存在")
    private String email;

    @TableField("phone")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @UniqueCheck(field = "phone", message = "手机号已存在")
    private String phone;

    @TableField("join_date")
    @NotNull(message = "入职日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDate;

    @TableField("probation_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @AssertTrue(message = "试用期结束日期必须晚于入职日期")
    private boolean isProbationEndDateValid() {
        if (probationEndDate == null || joinDate == null) {
            return true;
        }
        return probationEndDate.isAfter(joinDate);
    }

    // 外键约束
    @TableField("department_id")
    @NotNull(message = "部门不能为空")
    @ExistsCheck(table = "sys_department", field = "id", message = "部门不存在")
    private String departmentId;

    @TableField("manager_id")
    @ExistsCheck(table = "sys_employee", field = "id", message = "直属上级不存在")
    private String managerId;

    // 枚举约束
    @TableField("gender")
    @NotNull(message = "性别不能为空")
    private Gender gender;

    @TableField("status")
    @NotNull(message = "状态不能为空")
    private EmployeeStatus status;

    @TableField("probation_status")
    private ProbationStatus probationStatus;

    // 乐观锁
    @TableField("version")
    @Version
    private Integer version;

    // 逻辑删除
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
```

#### 5.1.2 部门表约束

```java
@TableName("sys_department")
@Data
public class Department {

    @TableId(value = "id", type = IdType.INPUT)
    @Pattern(regexp = "^DEPT\\d{4}$", message = "部门ID格式不正确")
    private String id;

    @TableField("name")
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 50, message = "部门名称长度在2-50个字符")
    private String name;

    @TableField("parent_id")
    @ParentCheck(message = "不能选择自己或子部门作为上级部门")
    private String parentId;

    @TableField("leader_id")
    @NotNull(message = "部门负责人不能为空")
    @ExistsCheck(table = "sys_employee", field = "id", message = "负责人不存在")
    private String leaderId;

    @TableField("level")
    @Min(value = 1, message = "部门层级不能小于1")
    @Max(value = 5, message = "部门层级不能大于5")
    private Integer level;

    // 检查约束: 同级部门名称唯一
    @AssertTrue(message = "同级部门名称已存在")
    private boolean isNameUnique() {
        // 由Service层实现检查逻辑
        return true;
    }
}
```

#### 5.1.3 资产表约束

```java
@TableName("biz_asset")
@Data
public class Asset {

    @TableId(value = "id", type = IdType.INPUT)
    @Pattern(regexp = "^ASSET\\d{6}$", message = "资产ID格式不正确")
    private String id;

    @TableField("name")
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 200, message = "资产名称最多200个字符")
    private String name;

    @TableField("purchase_price")
    @NotNull(message = "购置金额不能为空")
    @DecimalMin(value = "0.00", message = "购置金额不能为负数")
    private BigDecimal purchasePrice;

    @TableField("current_value")
    @DecimalMin(value = "0.00", message = "当前价值不能为负数")
    private BigDecimal currentValue;

    @TableField("user_id")
    @ExistsCheck(table = "sys_employee", field = "id", message = "使用人不存在")
    private String userId;

    @TableField("category")
    @NotNull(message = "资产类别不能为空")
    private AssetCategory category;

    @TableField("status")
    @NotNull(message = "资产状态不能为空")
    private AssetStatus status;

    @TableField("version")
    @Version
    private Integer version;
}
```

### 5.2 自定义验证注解

#### 5.2.1 唯一性检查注解

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCheckValidator.class)
public @interface UniqueCheck {
    String field();
    String message() default "数据已存在";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
@Component
public class UniqueCheckValidator implements ConstraintValidator<UniqueCheck, String> {

    @Autowired
    private EmployeeMapper employeeMapper;

    private String field;

    @Override
    public void initialize(UniqueCheck constraintAnnotation) {
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if ("email".equals(field)) {
            wrapper.eq(Employee::getEmail, value);
        } else if ("phone".equals(field)) {
            wrapper.eq(Employee::getPhone, value);
        }

        return employeeMapper.selectCount(wrapper) == 0;
    }
}
```

#### 5.2.2 存在性检查注解

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistsCheckValidator.class)
public @interface ExistsCheck {
    String table();
    String field();
    String message() default "数据不存在";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
@Component
public class ExistsCheckValidator implements ConstraintValidator<ExistsCheck, String> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String table;
    private String field;

    @Override
    public void initialize(ExistsCheck constraintAnnotation) {
        this.table = constraintAnnotation.table();
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND is_deleted = 0", table, field);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, value);
        return count != null && count > 0;
    }
}
```

### 5.3 Service层约束实现

Service层需要实现数据库中的外键约束和业务规则约束。

#### 5.3.1 员工Service约束

```java
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmployee(Employee employee) {
        // 1. 验证部门是否存在
        Department department = departmentMapper.selectById(employee.getDepartmentId());
        if (department == null || department.getIsDeleted()) {
            throw new BusinessException("部门不存在");
        }

        // 2. 如果有上级,验证上级是否存在
        if (StringUtils.isNotBlank(employee.getManagerId())) {
            Employee manager = employeeMapper.selectById(employee.getManagerId());
            if (manager == null || manager.getIsDeleted()) {
                throw new BusinessException("直属上级不存在");
            }

            // 3. 验证上级不能是自己
            if (employee.getManagerId().equals(employee.getId())) {
                throw new BusinessException("不能选择自己作为直属上级");
            }
        }

        // 4. 验证邮箱唯一性
        LambdaQueryWrapper<Employee> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(Employee::getEmail, employee.getEmail());
        if (employeeMapper.selectCount(emailWrapper) > 0) {
            throw new BusinessException("邮箱已被使用");
        }

        // 5. 验证手机号唯一性
        LambdaQueryWrapper<Employee> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(Employee::getPhone, employee.getPhone());
        if (employeeMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException("手机号已被使用");
        }

        // 6. 验证入职日期
        if (employee.getJoinDate().isAfter(LocalDate.now())) {
            throw new BusinessException("入职日期不能晚于今天");
        }

        // 7. 验证试用期日期
        if (employee.getProbationEndDate() != null &&
            !employee.getProbationEndDate().isAfter(employee.getJoinDate())) {
            throw new BusinessException("试用期结束日期必须晚于入职日期");
        }

        // 8. 自动计算工龄
        employee.setWorkYears(calculateWorkYears(employee.getJoinDate()));

        // 9. 保存员工
        employeeMapper.insert(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(Employee employee) {
        // 乐观锁验证
        Employee existingEmployee = employeeMapper.selectById(employee.getId());
        if (existingEmployee == null) {
            throw new BusinessException("员工不存在");
        }

        if (!existingEmployee.getVersion().equals(employee.getVersion())) {
            throw new BusinessException("数据已被修改,请刷新后重试");
        }

        // 其他验证...

        employee.setVersion(existingEmployee.getVersion() + 1);
        employeeMapper.updateById(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(String id) {
        // 1. 检查是否有下属员工
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getManagerId, id);
        Long count = employeeMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该员工有下属,无法删除");
        }

        // 2. 检查是否有借用资产
        // ...

        // 3. 逻辑删除
        employeeMapper.deleteById(id);
    }

    /**
     * 计算工龄
     */
    private Integer calculateWorkYears(LocalDate joinDate) {
        LocalDate now = LocalDate.now();
        int years = now.getYear() - joinDate.getYear();

        if (now.getMonthValue() < joinDate.getMonthValue()) {
            years--;
        } else if (now.getMonthValue() == joinDate.getMonthValue() &&
                   now.getDayOfMonth() < joinDate.getDayOfMonth()) {
            years--;
        }

        return Math.max(0, years);
    }
}
```

#### 5.3.2 部门Service约束

```java
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDepartment(Department department) {
        // 1. 如果有上级部门,验证上级是否存在
        if (StringUtils.isNotBlank(department.getParentId())) {
            Department parent = departmentMapper.selectById(department.getParentId());
            if (parent == null || parent.getIsDeleted()) {
                throw new BusinessException("上级部门不存在");
            }

            // 2. 计算层级
            department.setLevel(parent.getLevel() + 1);

            // 3. 验证层级不超过5级
            if (department.getLevel() > 5) {
                throw new BusinessException("部门层级不能超过5级");
            }
        } else {
            department.setLevel(1);
        }

        // 4. 验证部门负责人是否存在
        Employee leader = employeeMapper.selectById(department.getLeaderId());
        if (leader == null || leader.getIsDeleted()) {
            throw new BusinessException("部门负责人不存在");
        }

        // 5. 验证同级部门名称唯一性
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getName, department.getName())
               .eq(Department::getParentId, department.getParentId());
        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("同级部门名称已存在");
        }

        // 6. 保存部门
        departmentMapper.insert(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDepartment(String departmentId, String newParentId) {
        // 1. 不能移动到自己
        if (departmentId.equals(newParentId)) {
            throw new BusinessException("不能移动到自己");
        }

        // 2. 不能移动到自己的子部门
        List<String> childIds = getAllChildDepartmentIds(departmentId);
        if (childIds.contains(newParentId)) {
            throw new BusinessException("不能移动到自己的子部门");
        }

        // 3. 计算新层级
        Integer newLevel;
        if (StringUtils.isBlank(newParentId)) {
            newLevel = 1;
        } else {
            Department newParent = departmentMapper.selectById(newParentId);
            if (newParent == null) {
                throw new BusinessException("目标上级部门不存在");
            }
            newLevel = newParent.getLevel() + 1;
        }

        if (newLevel > 5) {
            throw new BusinessException("移动后层级将超过5级");
        }

        // 4. 更新部门及其所有子部门的层级
        updateDepartmentLevel(departmentId, newLevel);

        // 5. 更新上级部门
        Department department = departmentMapper.selectById(departmentId);
        department.setParentId(newParentId);
        department.setLevel(newLevel);
        departmentMapper.updateById(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(String id) {
        // 1. 检查是否有子部门
        LambdaQueryWrapper<Department> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Department::getParentId, id);
        Long childCount = departmentMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new BusinessException("该部门下还有子部门,请先删除或移动子部门");
        }

        // 2. 检查是否有成员
        LambdaQueryWrapper<Employee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.eq(Employee::getDepartmentId, id);
        Long empCount = employeeMapper.selectCount(empWrapper);
        if (empCount > 0) {
            throw new BusinessException("该部门下还有员工,请先转移或删除员工");
        }

        // 3. 逻辑删除
        departmentMapper.deleteById(id);
    }

    /**
     * 获取所有子部门ID
     */
    private List<String> getAllChildDepartmentIds(String parentId) {
        List<String> ids = new ArrayList<>();
        List<Department> children = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, parentId)
        );

        for (Department child : children) {
            ids.add(child.getId());
            ids.addAll(getAllChildDepartmentIds(child.getId()));
        }

        return ids;
    }

    /**
     * 更新部门层级(级联更新子部门)
     */
    private void updateDepartmentLevel(String departmentId, Integer newLevel) {
        Department department = new Department();
        department.setId(departmentId);
        department.setLevel(newLevel);
        departmentMapper.updateById(department);

        // 递归更新子部门
        List<Department> children = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, departmentId)
        );

        for (Department child : children) {
            updateDepartmentLevel(child.getId(), newLevel + 1);
        }
    }
}
```

---

## 6. 模块列表

| 模块 | 文档路径 | 状态 |
|------|---------|------|
| 通用组件 | [common/](./common/) | 待创建 |
| 员工管理 | [core/employee/](./core/employee/) | 待创建 |
| 部门管理 | [core/department/](./core/department/) | 待创建 |
| 资产管理 | [core/asset/](./core/asset/) | 待创建 |
| 请假审批 | [approval/leave/](./approval/leave/) | 待创建 |
| 费用报销 | [approval/expense/](./approval/expense/) | 待创建 |
| 会议室管理 | [admin/meeting/](./admin/meeting/) | 待创建 |
| 认证授权 | [auth/](./auth/) | 待创建 |

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
