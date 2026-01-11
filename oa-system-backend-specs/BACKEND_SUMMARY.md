# OA系统后端技术规范总结

> 基于前端规格 `oa-system-frontend-specs\` 和数据库规格 `oa-system-database-specs\` 创建
> **技术栈**: Spring Boot 3.x + MyBatis-Plus + MySQL 8.0+
> **版本**: v1.0.0

---

## ✅ 已完成的规范文档

### 1. 核心规范文档

| 文档 | 路径 | 说明 |
|------|------|------|
| 后端总规范 | [README.md](./README.md) | 项目架构、开发规范、约束实现 |
| 员工模块 | [core/employee/README.md](./core/employee/README.md) | 员工管理完整实现 |

---

## 📋 待完成的规范文档清单

### 2. 核心模块

| 模块 | 路径 | 对应前端 | 对应数据库 |
|------|------|---------|-----------|
| 部门管理 | [core/department/README.md](./core/department/) | [department_Technical.md](../oa-system-frontend-specs/core/department/) | [department.md](../oa-system-database-specs/01-core/department.md) |
| 资产管理 | [core/asset/README.md](./core/asset/) | [asset_Technical.md](../oa-system-frontend-specs/core/asset/) | [asset.md](../oa-system-database-specs/01-core/asset.md) |
| 数据字典 | [core/dict/README.md](./core/dict/) | [dict_Technical.md](../oa-system-frontend-specs/core/dict/) | (在menu.md中) |
| 菜单管理 | [core/menu/README.md](./core/menu/) | [menu_Technical.md](../oa-system-frontend-specs/core/menu/) | (在menu.md中) |

### 3. 审批模块

| 模块 | 路径 | 对应前端 | 对应数据库 |
|------|------|---------|-----------|
| 请假审批 | [approval/leave/README.md](./approval/leave/) | [leave_Technical.md](../oa-system-frontend-specs/approval/leave/) | (在leave_Technical.md中) |
| 费用报销 | [approval/expense/README.md](./approval/expense/) | [expense_Technical.md](../oa-system-frontend-specs/approval/expense/) | (在expense_Technical.md中) |

### 4. 管理模块

| 模块 | 路径 | 对应前端 | 对应数据库 |
|------|------|---------|-----------|
| 会议室管理 | [admin/meeting/README.md](./admin/meeting/) | [meeting_Technical.md](../oa-system-frontend-specs/admin/meeting/) | (在meeting_Technical.md中) |

### 5. 认证模块

| 模块 | 路径 | 对应前端 | 对应数据库 |
|------|------|---------|-----------|
| 登录认证 | [auth/README.md](./auth/) | [login_Technical.md](../oa-system-frontend-specs/auth/login/) | (在login_Technical.md中) |

### 6. 通用组件

| 组件 | 路径 | 说明 |
|------|------|------|
| 通用配置 | [common/config/README.md](./common/config/) | MyBatis-Plus、Redis、Swagger等配置 |
| 统一返回 | [common/result/README.md](./common/result/) | Result<T>统一返回格式 |
| 异常处理 | [common/exception/README.md](./common/exception/) | 全局异常处理 |
| 自定义注解 | [common/annotation/README.md](./common/annotation/) | 验证注解实现 |
| 工具类 | [common/util/README.md](./common/util/) | 常用工具类 |

---

## 🎯 核心设计要点总结

### 1. 数据库约束在代码中的实现

#### 1.1 实体层(Entity)约束

```java
@TableName("sys_employee")
@Data
public class Employee {

    // 主键约束
    @TableId(value = "id", type = IdType.INPUT)
    @Pattern(regexp = "^EMP\\d{15}$", message = "员工ID格式不正确")
    private String id;

    // 非空约束
    @TableField("name")
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符")
    private String name;

    // 唯一约束 + 格式约束
    @TableField("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @UniqueCheck(field = "email", message = "邮箱已被使用")
    private String email;

    // 外键约束
    @TableField("department_id")
    @NotNull(message = "部门不能为空")
    @ExistsCheck(table = "sys_department", field = "id", message = "部门不存在")
    private String departmentId;

    // 自定义验证约束
    @AssertTrue(message = "试用期结束日期必须晚于入职日期")
    public boolean isProbationEndDateValid() {
        if (probationEndDate == null || joinDate == null) {
            return true;
        }
        return probationEndDate.isAfter(joinDate);
    }

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

#### 1.2 Service层业务约束

```java
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmployee(Employee employee) {
        // 1. 外键约束验证
        Department department = departmentMapper.selectById(employee.getDepartmentId());
        if (department == null || department.getIsDeleted()) {
            throw new BusinessException("部门不存在");
        }

        // 2. 唯一性约束验证
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getEmail, employee.getEmail());
        if (employeeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("邮箱已被使用");
        }

        // 3. 业务规则验证
        if (employee.getJoinDate().isAfter(LocalDate.now())) {
            throw new BusinessException("入职日期不能晚于今天");
        }

        // 4. 自动计算字段
        employee.setWorkYears(calculateWorkYears(employee.getJoinDate()));

        // 5. 保存数据
        employeeMapper.insert(employee);
    }
}
```

### 2. 数据库约束映射表

| 数据库约束类型 | Java实现方式 | 示例 |
|--------------|-------------|------|
| PRIMARY KEY | @TableId | id字段 |
| FOREIGN KEY | @ExistsCheck + Service验证 | department_id |
| UNIQUE | @UniqueCheck + Service验证 | email, phone |
| NOT NULL | @NotNull / @NotBlank | name |
| CHECK | @Pattern / @Min / @Max | phone格式 |
| DEFAULT | 字段初始化 | status = "active" |
| INDEX | 无需实现 | 数据库层面 |
| 乐观锁 | @Version | version字段 |
| 逻辑删除 | @TableLogic | is_deleted字段 |

---

## 📦 项目结构总览

```
oa-system-backend/
├── src/main/java/com/oa/system/
│   ├── OaSystemApplication.java           # 启动类
│   │
│   ├── common/                             # 通用模块
│   │   ├── config/                         # 配置类
│   │   │   ├── MyBatisPlusConfig.java      # MyBatis-Plus配置
│   │   │   ├── RedisConfig.java            # Redis配置
│   │   │   ├── SwaggerConfig.java          # Swagger配置
│   │   │   └── WebMvcConfig.java           # Web MVC配置
│   │   ├── constant/                       # 常量定义
│   │   │   ├── CommonConstant.java         # 通用常量
│   │   │   └── CacheConstant.java          # 缓存常量
│   │   ├── exception/                      # 异常处理
│   │   │   ├── BusinessException.java      # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── result/                         # 统一返回结果
│   │   │   ├── Result.java                 # 返回结果封装
│   │   │   └── PageResult.java             # 分页结果
│   │   ├── util/                           # 工具类
│   │   │   ├── SecurityUtils.java          # 安全工具类
│   │   │   └── BeanUtils.java              # Bean工具类
│   │   └── annotation/                     # 自定义注解
│   │       ├── UniqueCheck.java            # 唯一性检查
│   │       └── ExistsCheck.java            # 存在性检查
│   │
│   ├── core/                               # 核心模块
│   │   ├── employee/                       # 员工管理 ✅
│   │   │   ├── controller/                 # 控制器
│   │   │   │   └── EmployeeController.java
│   │   │   ├── service/                    # 服务接口
│   │   │   │   ├── EmployeeService.java
│   │   │   │   └── impl/
│   │   │   │       └── EmployeeServiceImpl.java
│   │   │   ├── mapper/                     # 数据访问
│   │   │   │   ├── EmployeeMapper.java
│   │   │   │   └── EmployeeMapper.xml
│   │   │   ├── entity/                     # 实体类
│   │   │   │   ├── Employee.java
│   │   │   │   └── EmployeeOperationLog.java
│   │   │   ├── dto/                        # 数据传输对象
│   │   │   │   ├── EmployeeCreateDTO.java
│   │   │   │   ├── EmployeeUpdateDTO.java
│   │   │   │   ├── EmployeeQueryDTO.java
│   │   │   │   └── EmployeeStatusDTO.java
│   │   │   ├── vo/                         # 视图对象
│   │   │   │   ├── EmployeeVO.java
│   │   │   │   ├── EmployeeDetailVO.java
│   │   │   │   └── EmployeeStatisticsVO.java
│   │   │   ├── enums/                      # 枚举类
│   │   │   │   ├── EmployeeStatus.java
│   │   │   │   ├── ProbationStatus.java
│   │   │   │   └── Gender.java
│   │   │   └── schedule/                   # 定时任务
│   │   │       └── EmployeeSchedule.java
│   │   │
│   │   ├── department/                     # 部门管理 (待创建)
│   │   ├── asset/                          # 资产管理 (待创建)
│   │   ├── dict/                           # 数据字典 (待创建)
│   │   └── menu/                           # 菜单管理 (待创建)
│   │
│   ├── approval/                           # 审批模块
│   │   ├── leave/                          # 请假审批 (待创建)
│   │   └── expense/                        # 费用报销 (待创建)
│   │
│   ├── admin/                              # 管理模块
│   │   └── meeting/                        # 会议室管理 (待创建)
│   │
│   └── auth/                               # 认证授权 (待创建)
│       ├── controller/
│       ├── service/
│       ├── security/
│       └── filter/
│
└── src/main/resources/
    ├── mapper/                             # MyBatis XML
    │   ├── employee/
    │   │   └── EmployeeMapper.xml
    │   ├── department/
    │   ├── asset/
    │   ├── approval/
    │   └── auth/
    ├── application.yml                     # 主配置文件
    ├── application-dev.yml                 # 开发环境
    ├── application-test.yml                # 测试环境
    └── application-prod.yml                # 生产环境
```

---

## 🚀 快速开始

### 1. 创建Spring Boot项目

使用Spring Initializr创建项目:
- Spring Boot: 3.2.0
- Java: 17
- 依赖: Spring Web, MyBatis Framework, MySQL Driver, Validation

### 2. 添加依赖(pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- Hutool工具类 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.23</version>
    </dependency>

    <!-- Swagger/Knife4j -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>4.3.0</version>
    </dependency>
</dependencies>
```

### 3. 配置文件(application.yml)

```yaml
spring:
  application:
    name: oa-system
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/oa_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: input
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath:mapper/**/*.xml

knife4j:
  enable: true
  setting:
    language: zh_cn
```

### 4. 启动类

```java
package com.oa.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.oa.system.**.mapper")
public class OaSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaSystemApplication.class, args);
    }
}
```

---

## 📚 相关文档

- [前端规格文档](../oa-system-frontend-specs/README.md)
- [数据库规格文档](../oa-system-database-specs/README.md)
- [数据库约束脚本](../oa-system-database-specs/schemas/03_create_constraints.sql)

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
