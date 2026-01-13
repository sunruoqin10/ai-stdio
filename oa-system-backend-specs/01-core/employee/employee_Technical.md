# 员工管理后端技术实现规范

> **对应前端规范**: [employee_Technical.md](../../oa-system-frontend-specs/core/employee/employee_Technical.md)
> **数据库规范**: [employee.md](../../oa-system-database-specs/01-core/employee.md)
> **技术栈**: Spring Boot 3.x + MyBatis Plus + MySQL 8.0
> **版本**: v1.0.0
> **创建日期**: 2026-01-13

---

## 📋 目录

- [1. 项目结构](#1-项目结构)
- [2. 实体类设计](#2-实体类设计)
- [3. DTO设计](#3-dto设计)
- [4. VO设计](#4-vo设计)
- [5. Mapper接口](#5-mapper接口)
- [6. Service层设计](#6-service层设计)
- [7. Controller层设计](#7-controller层设计)
- [8. 业务逻辑实现](#8-业务逻辑实现)
- [9. 数据验证](#9-数据验证)
- [10. 权限控制](#10-权限控制)
- [11. 异常处理](#11-异常处理)
- [12. 定时任务](#12-定时任务)

---

## 1. 项目结构

```
oa-system-backend/src/main/java/com/example/oa_system_backend/module/employee/
├── controller/
│   └── EmployeeController.java          # 员工管理控制器
├── service/
│   ├── EmployeeService.java             # 员工服务接口
│   └── impl/
│       └── EmployeeServiceImpl.java      # 员工服务实现
├── mapper/
│   ├── EmployeeMapper.java              # 员工Mapper接口
│   └── EmployeeOperationLogMapper.java  # 操作日志Mapper
├── entity/
│   ├── Employee.java                     # 员工实体
│   └── EmployeeOperationLog.java        # 操作日志实体
├── dto/
│   ├── EmployeeCreateRequest.java       # 创建员工DTO
│   ├── EmployeeUpdateRequest.java       # 更新员工DTO
│   ├── EmployeeQueryRequest.java         # 查询员工DTO
│   ├── EmployeeStatusUpdateRequest.java # 状态更新DTO
│   └── EmployeeImportRequest.java       # 导入DTO
└── vo/
    ├── EmployeeVO.java                  # 员工视图对象
    ├── EmployeeDetailVO.java            # 员工详情VO
    ├── EmployeeStatisticsVO.java        # 统计VO
    └── OperationLogVO.java               # 操作日志VO
```

---

## 2. 实体类设计

### 2.1 Employee实体类

```java
package com.example.oa_system_backend.module.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 * 对应表: sys_employee
 */
@Data
@TableName("sys_employee")
public class Employee {

    /**
     * 主键: 员工编号
     * 格式: EMP+YYYYMMDD+序号
     */
    @TableId(type = IdType.INPUT)
    private String id;

    // ========== 基本信息 ==========

    /**
     * 姓名
     */
    private String name;

    /**
     * 英文名
     */
    private String englishName;

    /**
     * 性别: male-男, female-女
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    // ========== 工作信息 ==========

    /**
     * 部门ID
     * 外键关联 sys_department.id
     */
    @TableField("department_id")
    private String departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 职级
     */
    private String level;

    /**
     * 直属上级ID
     * 外键关联 sys_employee.id (自关联)
     */
    @TableField("manager_id")
    private String managerId;

    /**
     * 入职日期
     */
    @TableField("join_date")
    private LocalDate joinDate;

    /**
     * 试用期状态
     * probation-试用期内, regular-已转正, resigned-已离职
     */
    @TableField("probation_status")
    private String probationStatus;

    /**
     * 试用期结束日期
     */
    @TableField("probation_end_date")
    private LocalDate probationEndDate;

    /**
     * 工龄(年)
     * 自动计算
     */
    @TableField("work_years")
    private Integer workYears;

    // ========== 状态 ==========

    /**
     * 员工状态
     * active-在职, resigned-离职, suspended-停薪留职
     */
    private String status;

    // ========== 其他信息 ==========

    /**
     * 办公位置
     */
    @TableField("office_location")
    private String officeLocation;

    /**
     * 紧急联系人
     */
    @TableField("emergency_contact")
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    @TableField("emergency_phone")
    private String emergencyPhone;

    // ========== 审计字段 ==========

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 逻辑删除标记
     * 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @TableField("deleted_by")
    private String deletedBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    // ========== 关联对象(非数据库字段) ==========

    /**
     * 部门名称(关联查询)
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 上级姓名(关联查询)
     */
    @TableField(exist = false)
    private String managerName;
}
```

### 2.2 EmployeeOperationLog实体类

```java
package com.example.oa_system_backend.module.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工操作日志实体
 * 对应表: sys_employee_operation_log
 */
@Data
@TableName("sys_employee_operation_log")
public class EmployeeOperationLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 操作类型
     * CREATE-创建, UPDATE-更新, DELETE-删除, RESIGN-离职, etc.
     */
    private String operation;

    /**
     * 操作人ID
     */
    @TableField(exist = false)
    private String operator;

    /**
     * 操作人姓名
     */
    @TableField(exist = false)
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;

    /**
     * 详细信息(JSON格式)
     */
    private String details;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;
}
```

---

## 3. DTO设计

### 3.1 EmployeeCreateRequest

```java
package com.example.oa_system_backend.module.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建员工请求DTO
 */
@Data
public class EmployeeCreateRequest {

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符之间")
    private String name;

    /**
     * 英文名
     */
    @Size(max = 50, message = "英文名长度不能超过50个字符")
    private String englishName;

    /**
     * 性别
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别必须是male或female")
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 部门ID
     */
    @NotBlank(message = "部门不能为空")
    private String departmentId;

    /**
     * 职位
     */
    @NotBlank(message = "职位不能为空")
    @Size(min = 2, max = 50, message = "职位长度在2-50个字符之间")
    private String position;

    /**
     * 职级
     */
    private String level;

    /**
     * 直属上级ID
     */
    private String managerId;

    /**
     * 入职日期
     */
    @NotNull(message = "入职日期不能为空")
    @PastOrPresent(message = "入职日期不能晚于今天")
    private LocalDate joinDate;

    /**
     * 试用期结束日期
     */
    private LocalDate probationEndDate;

    /**
     * 办公位置
     */
    private String officeLocation;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String emergencyPhone;
}
```

### 3.2 EmployeeUpdateRequest

```java
package com.example.oa_system_backend.module.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新员工请求DTO
 */
@Data
public class EmployeeUpdateRequest {

    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符之间")
    private String name;

    @Size(max = 50, message = "英文名长度不能超过50个字符")
    private String englishName;

    @Pattern(regexp = "^(male|female)$", message = "性别必须是male或female")
    private String gender;

    private LocalDate birthDate;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "请输入正确的邮箱格式")
    private String email;

    private String avatar;

    private String departmentId;

    @Size(min = 2, max = 50, message = "职位长度在2-50个字符之间")
    private String position;

    private String level;

    private String managerId;

    private LocalDate joinDate;

    private LocalDate probationEndDate;

    private String officeLocation;

    private String emergencyContact;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String emergencyPhone;
}
```

### 3.3 EmployeeQueryRequest

```java
package com.example.oa_system_backend.module.employee.dto;

import lombok.Data;

/**
 * 员工查询请求DTO
 */
@Data
public class EmployeeQueryRequest {

    /**
     * 关键词搜索(姓名/工号/手机号)
     */
    private String keyword;

    /**
     * 员工状态
     */
    private String status;

    /**
     * 部门ID列表(逗号分隔)
     */
    private String departmentIds;

    /**
     * 职位
     */
    private String position;

    /**
     * 试用期状态
     */
    private String probationStatus;

    /**
     * 性别
     */
    private String gender;

    /**
     * 入职开始日期
     */
    private String joinDateStart;

    /**
     * 入职结束日期
     */
    private String joinDateEnd;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
```

### 3.4 EmployeeStatusUpdateRequest

```java
package com.example.oa_system_backend.module.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 员工状态更新请求DTO
 */
@Data
public class EmployeeStatusUpdateRequest {

    /**
     * 新状态
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|resigned|suspended)$",
             message = "状态必须是active、resigned或suspended")
    private String status;

    /**
     * 原因(离职原因等)
     */
    private String reason;
}
```

---

## 4. VO设计

### 4.1 EmployeeVO

```java
package com.example.oa_system_backend.module.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工视图对象
 */
@Data
public class EmployeeVO {

    /**
     * 员工编号
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 英文名
     */
    private String englishName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 职位
     */
    private String position;

    /**
     * 职级
     */
    private String level;

    /**
     * 直属上级ID
     */
    private String managerId;

    /**
     * 直属上级姓名
     */
    private String managerName;

    /**
     * 入职日期
     */
    private LocalDate joinDate;

    /**
     * 试用期状态
     */
    private String probationStatus;

    /**
     * 试用期结束日期
     */
    private LocalDate probationEndDate;

    /**
     * 工龄(年)
     */
    private Integer workYears;

    /**
     * 员工状态
     */
    private String status;

    /**
     * 办公位置
     */
    private String officeLocation;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
```

### 4.2 EmployeeStatisticsVO

```java
package com.example.oa_system_backend.module.employee.vo;

import lombok.Data;

import java.util.List;

/**
 * 员工统计数据VO
 */
@Data
public class EmployeeStatisticsVO {

    /**
     * 总员工数
     */
    private Integer total;

    /**
     * 在职人数
     */
    private Integer active;

    /**
     * 离职人数
     */
    private Integer resigned;

    /**
     * 停薪留职人数
     */
    private Integer suspended;

    /**
     * 试用期人数
     */
    private Integer probation;

    /**
     * 本月新入职
     */
    private Integer newThisMonth;

    /**
     * 部门分布
     */
    private List<DepartmentCountVO> byDepartment;

    /**
     * 部门人数统计
     */
    @Data
    public static class DepartmentCountVO {
        /**
         * 部门ID
         */
        private String departmentId;

        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 人数
         */
        private Integer count;
    }
}
```

### 4.3 OperationLogVO

```java
package com.example.oa_system_backend.module.employee.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 */
@Data
public class OperationLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private String employeeId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作人ID
     */
    private String operator;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;

    /**
     * 详细信息
     */
    private String details;

    /**
     * IP地址
     */
    private String ipAddress;
}
```

---

## 5. Mapper接口

### 5.1 EmployeeMapper

```java
package com.example.oa_system_backend.module.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.dto.EmployeeQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工Mapper接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 分页查询员工列表(带部门名称)
     */
    @Select("<script>" +
            "SELECT e.*, " +
            "       d.name AS department_name, " +
            "       m.name AS manager_name " +
            "FROM sys_employee e " +
            "LEFT JOIN sys_department d ON e.department_id = d.id AND d.is_deleted = 0 " +
            "LEFT JOIN sys_employee m ON e.manager_id = m.id AND m.is_deleted = 0 " +
            "WHERE e.is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (e.name LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR e.id LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR e.phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND e.status = #{status} " +
            "</if>" +
            "<if test='departmentIds != null and departmentIds != \"\"'>" +
            "  AND e.department_id IN " +
            "  <foreach collection='departmentIds.split(\",\")' item='deptId' open='(' separator=',' close=')'>" +
            "    #{deptId}" +
            "  </foreach>" +
            "</if>" +
            "<if test='position != null and position != \"\"'>" +
            "  AND e.position = #{position} " +
            "</if>" +
            "<if test='probationStatus != null and probationStatus != \"\"'>" +
            "  AND e.probation_status = #{probationStatus} " +
            "</if>" +
            "<if test='gender != null and gender != \"\"'>" +
            "  AND e.gender = #{gender} " +
            "</if>" +
            "<if test='joinDateStart != null and joinDateStart != \"\"'>" +
            "  AND e.join_date &gt;= #{joinDateStart} " +
            "</if>" +
            "<if test='joinDateEnd != null and joinDateEnd != \"\"'>" +
            "  AND e.join_date &lt;= #{joinDateEnd} " +
            "</if>" +
            "ORDER BY e.created_at DESC" +
            "</script>")
    IPage<EmployeeVO> selectPageWithDetails(
        Page<EmployeeVO> page,
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("departmentIds") String departmentIds,
        @Param("position") String position,
        @Param("probationStatus") String probationStatus,
        @Param("gender") String gender,
        @Param("joinDateStart") String joinDateStart,
        @Param("joinDateEnd") String joinDateEnd
    );

    /**
     * 根据入职日期范围查询员工数量
     * 用于生成员工编号
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE id LIKE CONCAT(#{dateStr}, '%') " +
            "AND is_deleted = 0")
    Integer countByJoinDate(@Param("dateStr") String dateStr);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE email = #{email} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>")
    Integer countByEmail(@Param("email") String email,
                          @Param("excludeId") String excludeId);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE phone = #{phone} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>")
    Integer countByPhone(@Param("phone") String phone,
                          @Param("excludeId") String excludeId);

    /**
     * 检查部门是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_department " +
            "WHERE id = #{departmentId} " +
            "AND is_deleted = 0")
    Integer countByDepartmentId(@Param("departmentId") String departmentId);

    /**
     * 检查上级是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE id = #{managerId} " +
            "AND is_deleted = 0 " +
            "AND status = 'active'")
    Integer countByManagerId(@Param("managerId") String managerId);
}
```

### 5.2 EmployeeOperationLogMapper

```java
package com.example.oa_system_backend.module.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.employee.entity.EmployeeOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工操作日志Mapper接口
 */
@Mapper
public interface EmployeeOperationLogMapper extends BaseMapper<EmployeeOperationLog> {
    // 使用MyBatis Plus提供的CRUD方法
}
```

---

## 6. Service层设计

### 6.1 EmployeeService接口

```java
package com.example.oa_system_backend.module.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.employee.dto.*;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.vo.*;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 分页查询员工列表
     */
    IPage<EmployeeVO> getEmployeeList(EmployeeQueryRequest request);

    /**
     * 根据ID获取员工详情
     */
    EmployeeDetailVO getEmployeeById(String id);

    /**
     * 创建员工
     */
    Employee createEmployee(EmployeeCreateRequest request);

    /**
     * 更新员工信息
     */
    Employee updateEmployee(String id, EmployeeUpdateRequest request);

    /**
     * 更新员工状态(办理离职等)
     */
    Employee updateEmployeeStatus(String id, EmployeeStatusUpdateRequest request);

    /**
     * 删除员工(逻辑删除)
     */
    void deleteEmployee(String id);

    /**
     * 获取员工统计数据
     */
    EmployeeStatisticsVO getStatistics();

    /**
     * 获取员工操作记录
     */
    IPage<OperationLogVO> getOperationLogs(String employeeId, Integer page, Integer pageSize);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email, String excludeId);

    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone, String excludeId);

    /**
     * 生成员工编号
     */
    String generateEmployeeId(LocalDate joinDate);

    /**
     * 计算工龄
     */
    Integer calculateWorkYears(LocalDate joinDate);

    /**
     * 计算试用期结束日期
     */
    LocalDate calculateProbationEndDate(LocalDate joinDate);
}
```

### 6.2 EmployeeServiceImpl实现类

**由于篇幅限制，关键实现逻辑在后续章节详细说明。**

---

## 7. Controller层设计

```java
package com.example.oa_system_backend.module.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.employee.dto.*;
import com.example.oa_system_backend.module.employee.service.EmployeeService;
import com.example.oa_system_backend.module.employee.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工管理控制器
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 分页查询员工列表
     * GET /api/employees
     */
    @GetMapping
    public ApiResponse<IPage<EmployeeVO>> getEmployeeList(EmployeeQueryRequest request) {
        Page<EmployeeVO> page = new Page<>(request.getPage(), request.getPageSize());
        IPage<EmployeeVO> result = employeeService.getEmployeeList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取员工详情
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<EmployeeDetailVO> getEmployeeById(@PathVariable String id) {
        EmployeeDetailVO employee = employeeService.getEmployeeById(id);
        return ApiResponse.success(employee);
    }

    /**
     * 创建员工
     * POST /api/employees
     */
    @PostMapping
    public ApiResponse<Employee> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request,
            HttpServletRequest httpRequest) {
        Employee employee = employeeService.createEmployee(request);
        return ApiResponse.success("创建成功", employee);
    }

    /**
     * 更新员工信息
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<Employee> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        Employee employee = employeeService.updateEmployee(id, request);
        return ApiResponse.success("更新成功", employee);
    }

    /**
     * 更新员工状态
     * PUT /api/employees/{id}/status
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Employee> updateEmployeeStatus(
            @PathVariable String id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request) {
        Employee employee = employeeService.updateEmployeeStatus(id, request);
        return ApiResponse.success("状态更新成功", employee);
    }

    /**
     * 删除员工
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取统计数据
     * GET /api/employees/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<EmployeeStatisticsVO> getStatistics() {
        EmployeeStatisticsVO statistics = employeeService.getStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取操作记录
     * GET /api/employees/{id}/logs
     */
    @GetMapping("/{id}/logs")
    public ApiResponse<IPage<OperationLogVO>> getOperationLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // 实现略...
        return ApiResponse.success();
    }

    /**
     * 检查邮箱是否存在
     * GET /api/employees/check-email
     */
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmailExists(
            @RequestParam String email,
            @RequestParam(required = false) String excludeId) {
        boolean exists = employeeService.checkEmailExists(email, excludeId);
        return ApiResponse.success(exists);
    }

    /**
     * 检查手机号是否存在
     * GET /api/employees/check-phone
     */
    @GetMapping("/check-phone")
    public ApiResponse<Boolean> checkPhoneExists(
            @RequestParam String phone,
            @RequestParam(required = false) String excludeId) {
        boolean exists = employeeService.checkPhoneExists(phone, excludeId);
        return ApiResponse.success(exists);
    }
}
```

---

## 8. 业务逻辑实现

### 8.1 员工编号生成

```java
@Override
@Transactional
public String generateEmployeeId(LocalDate joinDate) {
    // 格式化日期为 YYYYMMDD
    String dateStr = joinDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    // 查询当天入职人数
    Integer count = employeeMapper.countByJoinDate(dateStr);

    // 生成3位序号
    String sequence = String.format("%03d", count + 1);

    // 返回员工编号: EMP + YYYYMMDD + 序号
    return "EMP" + dateStr + sequence;
}
```

### 8.2 工龄计算

```java
@Override
public Integer calculateWorkYears(LocalDate joinDate) {
    LocalDate now = LocalDate.now();

    // 计算年份差
    int years = now.getYear() - joinDate.getYear();

    // 如果还没到入职月份,减1年
    if (now.getMonthValue() < joinDate.getMonthValue()) {
        years--;
    }
    // 如果是入职月份但还没到入职日,减1年
    else if (now.getMonthValue() == joinDate.getMonthValue() &&
             now.getDayOfMonth() < joinDate.getDayOfMonth()) {
        years--;
    }

    return Math.max(0, years);
}
```

### 8.3 试用期结束日期计算

```java
@Override
public LocalDate calculateProbationEndDate(LocalDate joinDate) {
    // 默认试用期3个月
    return joinDate.plusMonths(3);
}
```

### 8.4 创建员工业务逻辑

```java
@Override
@Transactional
public Employee createEmployee(EmployeeCreateRequest request) {
    // 1. 验证邮箱唯一性
    if (employeeMapper.countByEmail(request.getEmail(), null) > 0) {
        throw new BusinessException("该邮箱已被使用");
    }

    // 2. 验证手机号唯一性
    if (employeeMapper.countByPhone(request.getPhone(), null) > 0) {
        throw new BusinessException("该手机号已被使用");
    }

    // 3. 验证部门存在性
    if (employeeMapper.countByDepartmentId(request.getDepartmentId()) == 0) {
        throw new BusinessException("指定的部门不存在");
    }

    // 4. 验证上级存在性(如果指定)
    if (request.getManagerId() != null &&
        employeeMapper.countByManagerId(request.getManagerId()) == 0) {
        throw new BusinessException("指定的直属上级不存在或已离职");
    }

    // 5. 生成员工编号
    String employeeId = generateEmployeeId(request.getJoinDate());

    // 6. 计算工龄
    Integer workYears = calculateWorkYears(request.getJoinDate());

    // 7. 计算试用期结束日期(如果未指定)
    LocalDate probationEndDate = request.getProbationEndDate();
    if (probationEndDate == null) {
        probationEndDate = calculateProbationEndDate(request.getJoinDate());
    }

    // 8. 构建Employee实体
    Employee employee = new Employee();
    BeanUtils.copyProperties(request, employee);
    employee.setId(employeeId);
    employee.setWorkYears(workYears);
    employee.setProbationEndDate(probationEndDate);
    employee.setStatus("active");
    employee.setProbationStatus("probation");
    employee.setCreatedAt(LocalDateTime.now());
    employee.setUpdatedAt(LocalDateTime.now());

    // 9. 保存到数据库
    employeeMapper.insert(employee);

    // 10. 记录操作日志
    saveOperationLog(employeeId, "CREATE", "创建员工", null);

    return employee;
}
```

---

## 9. 数据验证

### 9.1 外键约束验证

```java
/**
 * 验证部门存在性
 */
private void validateDepartmentId(String departmentId) {
    if (employeeMapper.countByDepartmentId(departmentId) == 0) {
        throw new BusinessException("指定的部门不存在");
    }
}

/**
 * 验证上级存在性
 */
private void validateManagerId(String managerId) {
    if (managerId != null &&
        employeeMapper.countByManagerId(managerId) == 0) {
        throw new BusinessException("指定的直属上级不存在或已离职");
    }
}

/**
 * 验证邮箱唯一性
 */
private void validateEmailUnique(String email, String excludeId) {
    if (employeeMapper.countByEmail(email, excludeId) > 0) {
        throw new BusinessException("该邮箱已被使用");
    }
}

/**
 * 验证手机号唯一性
 */
private void validatePhoneUnique(String phone, String excludeId) {
    if (employeeMapper.countByPhone(phone, excludeId) > 0) {
        throw new BusinessException("该手机号已被使用");
    }
}
```

### 9.2 数据完整性验证

```java
/**
 * 验证入职日期不能晚于今天
 */
private void validateJoinDate(LocalDate joinDate) {
    if (joinDate.isAfter(LocalDate.now())) {
        throw new BusinessException("入职日期不能晚于今天");
    }
}

/**
 * 验证试用期结束日期必须晚于入职日期
 */
private void validateProbationEndDate(LocalDate joinDate, LocalDate probationEndDate) {
    if (probationEndDate != null && !probationEndDate.isAfter(joinDate)) {
        throw new BusinessException("试用期结束日期必须晚于入职日期");
    }
}
```

---

## 10. 权限控制

### 10.1 Spring Security配置

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 员工管理权限
                .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyAuthority(
                    "employee:view", "employee:view_all", "employee:view_department"
                )
                .requestMatchers(HttpMethod.POST, "/api/employees").hasAuthority("employee:create")
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyAuthority(
                    "employee:edit", "employee:edit_all"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAuthority("employee:delete")
                .requestMatchers(HttpMethod.PUT, "/api/employees/*/status").hasAuthority("employee:resign")
                .requestMatchers(HttpMethod.GET, "/api/employees/statistics").hasAnyAuthority(
                    "employee:view", "employee:view_all", "employee:view_department"
                )
                // 其他请求...
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

### 10.2 数据权限过滤

```java
/**
 * 获取当前用户的数据权限过滤条件
 */
private QueryWrapper<Employee> getDataPermissionFilter(QueryWrapper<Employee> wrapper) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
        return wrapper;
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String userId = userDetails.getUsername();

    // 获取用户权限
    Set<String> authorities = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    // 系统管理员可以查看所有数据
    if (authorities.contains("employee:view_all")) {
        return wrapper;
    }

    // 部门管理员只能查看本部门数据
    if (authorities.contains("employee:view_department")) {
        Employee currentUser = employeeMapper.selectById(userId);
        if (currentUser != null) {
            wrapper.eq("department_id", currentUser.getDepartmentId());
        }
        return wrapper;
    }

    // 普通员工只能查看自己
    if (authorities.contains("employee:view")) {
        wrapper.eq("id", userId);
    }

    return wrapper;
}
```

---

## 11. 异常处理

### 11.1 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.error("参数验证失败: {}", message);
        return ApiResponse.error("参数验证失败: " + message);
    }

    /**
     * 资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源不存在: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }
}
```

---

## 12. 定时任务

### 12.1 工龄自动更新任务

```java
@Component
@Slf4j
public class WorkYearUpdateTask {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 每月1号凌晨1点更新工龄
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void updateAllWorkYears() {
        log.info("开始更新所有员工工龄");

        // 查询所有在职员工
        List<Employee> employees = employeeService.getAllActiveEmployees();

        int updateCount = 0;
        for (Employee employee : employees) {
            try {
                Integer newWorkYears = employeeService.calculateWorkYears(employee.getJoinDate());

                if (!newWorkYears.equals(employee.getWorkYears())) {
                    employee.setWorkYears(newWorkYears);
                    employeeService.updateById(employee);
                    updateCount++;
                }
            } catch (Exception e) {
                log.error("更新员工 {} 工龄失败: {}", employee.getId(), e.getMessage());
            }
        }

        log.info("工龄更新完成，共更新 {} 条记录", updateCount);
    }
}
```

### 12.2 生日提醒任务

```java
@Component
@Slf4j
public class BirthdayReminderTask {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 每天早上9点检查生日提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkBirthdayReminders() {
        log.info("开始检查生日提醒");

        // 查询今天生日的在职员工
        List<Employee> birthdayEmployees =
            employeeService.getTodayBirthdayEmployees();

        if (!birthdayEmployees.isEmpty()) {
            for (Employee employee : birthdayEmployees) {
                // 发送生日祝福通知
                sendBirthdayWish(employee);
            }

            log.info("今日有 {} 位员工过生日", birthdayEmployees.size());
        } else {
            log.info("今日没有员工过生日");
        }
    }

    private void sendBirthdayWish(Employee employee) {
        // TODO: 实现发送通知逻辑
        log.info("发送生日祝福给: {} ({})",
            employee.getName(), employee.getEmail());
    }
}
```

### 12.3 转正提醒任务

```java
@Component
@Slf4j
public class ProbationReminderTask {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 每天早上9点检查转正提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkProbationReminders() {
        log.info("开始检查转正提醒");

        // 查询7天内试用期到期的员工
        List<Employee> expiringEmployees =
            employeeService.getProbationExpiringEmployees(7);

        if (!expiringEmployees.isEmpty()) {
            for (Employee employee : expiringEmployees) {
                // 发送转正提醒
                sendProbationReminder(employee);
            }

            log.info("有 {} 位员工试用期即将到期", expiringEmployees.size());
        } else {
            log.info("没有即将到期试用期员工");
        }
    }

    private void sendProbationReminder(Employee employee) {
        // TODO: 实现发送提醒逻辑
        log.info("发送转正提醒给: {} ({})",
            employee.getName(), employee.getEmail());
    }
}
```

---

## 附录

### A. 数据库映射注意事项

1. **字段映射**: 使用`@TableField`注解明确数据库字段名
2. **逻辑删除**: 使用`@TableLogic`注解标记`is_deleted`字段
3. **乐观锁**: 使用`@Version`注解实现乐观锁控制
4. **自关联**: manager_id字段需要特殊处理

### B. 性能优化建议

1. **索引优化**: 确保常用查询字段都有索引
2. **分页查询**: 使用MyBatis Plus的分页插件
3. **缓存策略**: 对字典数据进行缓存
4. **批量操作**: 使用批量插入/更新减少数据库交互

### C. 测试要点

- [ ] 员工CRUD功能完整性
- [ ] 外键约束验证
- [ ] 唯一性约束验证
- [ ] 权限控制有效性
- [ ] 工龄计算准确性
- [ ] 员工编号生成规则
- [ ] 试用期计算正确性

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-13
