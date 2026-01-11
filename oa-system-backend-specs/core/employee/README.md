# 员工管理后端技术规范

> **对应前端规范**: [employee_Technical.md](../../../oa-system-frontend-specs/core/employee/employee_Technical.md)
> **对应数据库规范**: [employee.md](../../../oa-system-database-specs/01-core/employee.md)
> **技术栈**: Spring Boot 3.x + MyBatis-Plus + MySQL 8.0
> **版本**: v1.0.0

---

## 📋 目录

- [1. 数据库约束实现](#1-数据库约束实现)
- [2. Entity实体设计](#2-entity实体设计)
- [3. Mapper数据访问](#3-mapper数据访问)
- [4. Service业务逻辑](#4-service业务逻辑)
- [5. Controller接口设计](#5-controller接口设计)
- [6. DTO数据传输对象](#6-dto数据传输对象)
- [7. 异常处理](#7-异常处理)
- [8. 定时任务](#8-定时任务)

---

## 1. 数据库约束实现

根据数据库约束规范 `03_create_constraints.sql`,在Java代码中实现对应约束。

### 1.1 员工表约束映射

| 数据库约束 | Java实现 | 位置 |
|-----------|---------|------|
| `chk_email_format` | `@Email` | Entity |
| `chk_phone_format` | `@Pattern(regexp="^1[3-9]\\d{9}$")` | Entity |
| `chk_join_date` | Service层验证 | Service |
| `chk_probation_date` | 自定义验证方法 | Entity |
| `uk_employee_email` | `@UniqueCheck` | Entity + Service |
| `uk_employee_phone` | `@UniqueCheck` | Entity + Service |
| 外键约束 | Service层验证 | Service |
| 乐观锁 | `@Version` | Entity |
| 逻辑删除 | `@TableLogic` | Entity |

---

## 2. Entity实体设计

### 2.1 Employee实体

```java
package com.oa.system.core.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oa.system.common.validation.ExistsCheck;
import com.oa.system.common.validation.UniqueCheck;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体
 * 对应表: sys_employee
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_employee")
public class Employee {

    /**
     * 员工编号 - 主键
     */
    @TableId(value = "id", type = IdType.INPUT)
    @Pattern(regexp = "^EMP\\d{15}$", message = "员工ID格式不正确")
    private String id;

    /**
     * 姓名
     */
    @TableField("name")
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符")
    private String name;

    /**
     * 英文名
     */
    @TableField("english_name")
    @Size(max = 50, message = "英文名最多50个字符")
    private String englishName;

    /**
     * 性别
     */
    @TableField("gender")
    @NotNull(message = "性别不能为空")
    private Gender gender;

    /**
     * 出生日期
     */
    @TableField("birth_date")
    private LocalDate birthDate;

    /**
     * 联系电话
     * 约束: 唯一 + 格式验证
     */
    @TableField("phone")
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @UniqueCheck(field = "phone", message = "手机号已被使用")
    private String phone;

    /**
     * 邮箱
     * 约束: 唯一 + 格式验证
     */
    @TableField("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @UniqueCheck(field = "email", message = "邮箱已被使用")
    private String email;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 部门ID
     * 约束: 外键 -> sys_department.id
     */
    @TableField("department_id")
    @NotNull(message = "部门不能为空")
    @ExistsCheck(table = "sys_department", field = "id", message = "部门不存在")
    private String departmentId;

    /**
     * 职位
     */
    @TableField("position")
    @NotBlank(message = "职位不能为空")
    @Size(min = 2, max = 50, message = "职位长度在2-50个字符")
    private String position;

    /**
     * 职级
     */
    @TableField("level")
    @Size(max = 50, message = "职级最多50个字符")
    private String level;

    /**
     * 直属上级ID
     * 约束: 外键 -> sys_employee.id (自关联)
     */
    @TableField("manager_id")
    @ExistsCheck(table = "sys_employee", field = "id", message = "直属上级不存在")
    private String managerId;

    /**
     * 入职日期
     * 约束: 不能晚于今天
     */
    @TableField("join_date")
    @NotNull(message = "入职日期不能为空")
    private LocalDate joinDate;

    /**
     * 试用期状态
     */
    @TableField("probation_status")
    private ProbationStatus probationStatus;

    /**
     * 试用期结束日期
     * 约束: 必须晚于入职日期
     */
    @TableField("probation_end_date")
    private LocalDate probationEndDate;

    /**
     * 工龄(年)
     * 自动计算
     */
    @TableField("work_years")
    private Integer workYears;

    /**
     * 员工状态
     */
    @TableField("status")
    @NotNull(message = "员工状态不能为空")
    private EmployeeStatus status;

    /**
     * 办公位置
     */
    @TableField("office_location")
    @Size(max = 200, message = "办公位置最多200个字符")
    private String officeLocation;

    /**
     * 紧急联系人
     */
    @TableField("emergency_contact")
    @Size(max = 50, message = "紧急联系人最多50个字符")
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    @TableField("emergency_phone")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系电话格式不正确")
    private String emergencyPhone;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 是否删除
     * 逻辑删除标记
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    /**
     * 乐观锁版本号
     */
    @TableField("version")
    @Version
    private Integer version;

    /**
     * 自定义验证: 试用期结束日期必须晚于入职日期
     */
    @AssertTrue(message = "试用期结束日期必须晚于入职日期")
    public boolean isProbationEndDateValid() {
        if (probationEndDate == null || joinDate == null) {
            return true;
        }
        return probationEndDate.isAfter(joinDate);
    }
}
```

### 2.2 枚举定义

```java
package com.oa.system.core.employee.enums;

import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum Gender {
    MALE("male", "男"),
    FEMALE("female", "女");

    private final String code;
    private final String description;

    Gender(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
```

```java
package com.oa.system.core.employee.enums;

import lombok.Getter;

/**
 * 员工状态枚举
 */
@Getter
public enum EmployeeStatus {
    ACTIVE("active", "在职"),
    RESIGNED("resigned", "离职"),
    SUSPENDED("suspended", "停薪留职");

    private final String code;
    private final String description;

    EmployeeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
```

```java
package com.oa.system.core.employee.enums;

import lombok.Getter;

/**
 * 试用期状态枚举
 */
@Getter
public enum ProbationStatus {
    PROBATION("probation", "试用期内"),
    REGULAR("regular", "已转正"),
    RESIGNED("resigned", "已离职");

    private final String code;
    private final String description;

    ProbationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
```

### 2.3 操作日志实体

```java
package com.oa.system.core.employee.entity;

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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private String employeeId;

    @TableField("operation")
    private String operation;

    @TableField("operator")
    private String operator;

    @TableField("operator_name")
    private String operatorName;

    @TableField("timestamp")
    private LocalDateTime timestamp;

    @TableField("details")
    private String details;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;
}
```

---

## 3. Mapper数据访问

### 3.1 EmployeeMapper接口

```java
package com.oa.system.core.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.core.employee.entity.Employee;
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
     * 根据出生日期查询当天生日的员工
     */
    @Select("SELECT * FROM sys_employee " +
            "WHERE DAY(birth_date) = DAY(#{date}) " +
            "AND MONTH(birth_date) = MONTH(#{date}) " +
            "AND status = 'active' " +
            "AND is_deleted = 0")
    List<Employee> selectByBirthDate(@Param("date") LocalDate date);

    /**
     * 查询试用期即将到期的员工
     */
    @Select("SELECT * FROM sys_employee " +
            "WHERE probation_status = 'probation' " +
            "AND probation_end_date BETWEEN #{startDate} AND #{endDate} " +
            "AND is_deleted = 0")
    List<Employee> selectByProbationEndDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 统计部门员工数
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE department_id = #{deptId} " +
            "AND status = 'active' " +
            "AND is_deleted = 0")
    Long countByDepartmentId(@Param("deptId") String deptId);

    /**
     * 查询部门所有员工(包含子部门)
     */
    List<Employee> selectByDepartmentIds(@Param("deptIds") List<String> deptIds);
}
```

### 3.2 EmployeeMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.oa.system.core.employee.mapper.EmployeeMapper">

    <!-- 查询部门所有员工(包含子部门) -->
    <select id="selectByDepartmentIds" resultType="com.oa.system.core.employee.entity.Employee">
        SELECT *
        FROM sys_employee
        WHERE department_id IN
        <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
            #{deptId}
        </foreach>
        AND status = 'active'
        AND is_deleted = 0
        ORDER BY created_at DESC
    </select>

</mapper>
```

### 3.3 EmployeeOperationLogMapper

```java
package com.oa.system.core.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.core.employee.entity.EmployeeOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工操作日志Mapper
 */
@Mapper
public interface EmployeeOperationLogMapper extends BaseMapper<EmployeeOperationLog> {
}
```

---

## 4. Service业务逻辑

### 4.1 EmployeeService接口

```java
package com.oa.system.core.employee.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.system.core.employee.entity.Employee;
import com.oa.system.core.employee.dto.*;

/**
 * 员工Service接口
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 分页查询员工列表
     */
    Page<EmployeeVO> pageEmployee(EmployeeQueryDTO queryDTO);

    /**
     * 获取员工详情
     */
    EmployeeDetailVO getEmployeeDetail(String id);

    /**
     * 创建员工
     */
    String createEmployee(EmployeeCreateDTO createDTO);

    /**
     * 更新员工信息
     */
    void updateEmployee(EmployeeUpdateDTO updateDTO);

    /**
     * 更新员工状态
     */
    void updateEmployeeStatus(String id, EmployeeStatusDTO statusDTO);

    /**
     * 删除员工
     */
    void deleteEmployee(String id);

    /**
     * 批量删除员工
     */
    void batchDeleteEmployees(List<String> ids);

    /**
     * 获取员工统计数据
     */
    EmployeeStatisticsVO getStatistics();

    /**
     * 批量导入员工
     */
    ImportResultVO importEmployees(MultipartFile file);

    /**
     * 导出员工列表
     */
    void exportEmployees(EmployeeQueryDTO queryDTO, HttpServletResponse response);
}
```

### 4.2 EmployeeServiceImpl实现

```java
package com.oa.system.core.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.system.common.exception.BusinessException;
import com.oa.system.common.util.SecurityUtils;
import com.oa.system.core.department.entity.Department;
import com.oa.system.core.department.mapper.DepartmentMapper;
import com.oa.system.core.employee.dto.*;
import com.oa.system.core.employee.entity.Employee;
import com.oa.system.core.employee.entity.EmployeeOperationLog;
import com.oa.system.core.employee.enums.EmployeeStatus;
import com.oa.system.core.employee.mapper.EmployeeMapper;
import com.oa.system.core.employee.mapper.EmployeeOperationLogMapper;
import com.oa.system.core.employee.service.EmployeeService;
import com.oa.system.core.employee.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
        implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeOperationLogMapper operationLogMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public Page<EmployeeVO> pageEmployee(EmployeeQueryDTO queryDTO) {
        Page<Employee> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Employee::getName, queryDTO.getKeyword())
                    .or().like(Employee::getId, queryDTO.getKeyword())
                    .or().like(Employee::getPhone, queryDTO.getKeyword()));
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Employee::getStatus, queryDTO.getStatus());
        }

        // 部门筛选
        if (queryDTO.getDepartmentIds() != null && !queryDTO.getDepartmentIds().isEmpty()) {
            wrapper.in(Employee::getDepartmentId, queryDTO.getDepartmentIds());
        }

        // 职位筛选
        if (StringUtils.hasText(queryDTO.getPosition())) {
            wrapper.eq(Employee::getPosition, queryDTO.getPosition());
        }

        // 试用期状态筛选
        if (queryDTO.getProbationStatus() != null) {
            wrapper.eq(Employee::getProbationStatus, queryDTO.getProbationStatus());
        }

        // 性别筛选
        if (queryDTO.getGender() != null) {
            wrapper.eq(Employee::getGender, queryDTO.getGender());
        }

        // 入职时间范围
        if (queryDTO.getJoinDateStart() != null) {
            wrapper.ge(Employee::getJoinDate, queryDTO.getJoinDateStart());
        }
        if (queryDTO.getJoinDateEnd() != null) {
            wrapper.le(Employee::getJoinDate, queryDTO.getJoinDateEnd());
        }

        wrapper.orderByDesc(Employee::getCreatedAt);

        Page<Employee> employeePage = employeeMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<EmployeeVO> voPage = new Page<>(employeePage.getCurrent(), employeePage.getSize(), employeePage.getTotal());
        List<EmployeeVO> voList = convertToVOList(employeePage.getRecords());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public EmployeeDetailVO getEmployeeDetail(String id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null || employee.getIsDeleted()) {
            throw new BusinessException("员工不存在");
        }

        EmployeeDetailVO detailVO = new EmployeeDetailVO();
        BeanUtils.copyProperties(employee, detailVO);

        // 查询部门信息
        Department department = departmentMapper.selectById(employee.getDepartmentId());
        if (department != null) {
            detailVO.setDepartmentName(department.getName());
        }

        // 查询上级信息
        if (StringUtils.hasText(employee.getManagerId())) {
            Employee manager = employeeMapper.selectById(employee.getManagerId());
            if (manager != null) {
                detailVO.setManagerName(manager.getName());
                detailVO.setManagerPosition(manager.getPosition());
            }
        }

        // 查询操作日志
        LambdaQueryWrapper<EmployeeOperationLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(EmployeeOperationLog::getEmployeeId, id)
                .orderByDesc(EmployeeOperationLog::getTimestamp)
                .last("LIMIT 10");
        List<EmployeeOperationLog> logs = operationLogMapper.selectList(logWrapper);
        detailVO.setLogs(convertLogToVOList(logs));

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createEmployee(EmployeeCreateDTO createDTO) {
        // 1. 验证部门是否存在
        Department department = departmentMapper.selectById(createDTO.getDepartmentId());
        if (department == null || department.getIsDeleted()) {
            throw new BusinessException("部门不存在");
        }

        // 2. 如果有上级,验证上级是否存在且不能是自己
        if (StringUtils.hasText(createDTO.getManagerId())) {
            if (createDTO.getManagerId().equals(createDTO.getId())) {
                throw new BusinessException("不能选择自己作为直属上级");
            }

            Employee manager = employeeMapper.selectById(createDTO.getManagerId());
            if (manager == null || manager.getIsDeleted()) {
                throw new BusinessException("直属上级不存在");
            }
        }

        // 3. 验证邮箱唯一性
        LambdaQueryWrapper<Employee> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(Employee::getEmail, createDTO.getEmail());
        if (employeeMapper.selectCount(emailWrapper) > 0) {
            throw new BusinessException("邮箱已被使用");
        }

        // 4. 验证手机号唯一性
        LambdaQueryWrapper<Employee> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(Employee::getPhone, createDTO.getPhone());
        if (employeeMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException("手机号已被使用");
        }

        // 5. 验证入职日期不能晚于今天
        if (createDTO.getJoinDate().isAfter(LocalDate.now())) {
            throw new BusinessException("入职日期不能晚于今天");
        }

        // 6. 验证试用期结束日期必须晚于入职日期
        if (createDTO.getProbationEndDate() != null &&
            !createDTO.getProbationEndDate().isAfter(createDTO.getJoinDate())) {
            throw new BusinessException("试用期结束日期必须晚于入职日期");
        }

        // 7. 转换DTO到Entity
        Employee employee = new Employee();
        BeanUtils.copyProperties(createDTO, employee);

        // 8. 自动计算工龄
        employee.setWorkYears(calculateWorkYears(createDTO.getJoinDate()));

        // 9. 设置默认值
        if (employee.getProbationStatus() == null) {
            employee.setProbationStatus(ProbationStatus.REGULAR);
        }
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        // 10. 保存员工
        employeeMapper.insert(employee);

        // 11. 记录操作日志
        saveOperationLog(employee.getId(), "创建员工", "创建员工: " + employee.getName());

        log.info("创建员工成功: {}", employee.getId());
        return employee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(EmployeeUpdateDTO updateDTO) {
        // 1. 查询现有员工
        Employee existingEmployee = employeeMapper.selectById(updateDTO.getId());
        if (existingEmployee == null || existingEmployee.getIsDeleted()) {
            throw new BusinessException("员工不存在");
        }

        // 2. 乐观锁验证
        if (!existingEmployee.getVersion().equals(updateDTO.getVersion())) {
            throw new BusinessException("数据已被修改,请刷新后重试");
        }

        // 3. 验证部门是否存在
        if (StringUtils.hasText(updateDTO.getDepartmentId())) {
            Department department = departmentMapper.selectById(updateDTO.getDepartmentId());
            if (department == null || department.getIsDeleted()) {
                throw new BusinessException("部门不存在");
            }
        }

        // 4. 如果有上级,验证上级是否存在且不能是自己
        if (StringUtils.hasText(updateDTO.getManagerId())) {
            if (updateDTO.getManagerId().equals(updateDTO.getId())) {
                throw new BusinessException("不能选择自己作为直属上级");
            }

            Employee manager = employeeMapper.selectById(updateDTO.getManagerId());
            if (manager == null || manager.getIsDeleted()) {
                throw new BusinessException("直属上级不存在");
            }
        }

        // 5. 验证邮箱唯一性(排除自己)
        if (StringUtils.hasText(updateDTO.getEmail()) &&
            !updateDTO.getEmail().equals(existingEmployee.getEmail())) {

            LambdaQueryWrapper<Employee> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(Employee::getEmail, updateDTO.getEmail())
                    .ne(Employee::getId, updateDTO.getId());
            if (employeeMapper.selectCount(emailWrapper) > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }

        // 6. 验证手机号唯一性(排除自己)
        if (StringUtils.hasText(updateDTO.getPhone()) &&
            !updateDTO.getPhone().equals(existingEmployee.getPhone())) {

            LambdaQueryWrapper<Employee> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(Employee::getPhone, updateDTO.getPhone())
                    .ne(Employee::getId, updateDTO.getId());
            if (employeeMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException("手机号已被使用");
            }
        }

        // 7. 验证试用期结束日期必须晚于入职日期
        if (updateDTO.getProbationEndDate() != null &&
            existingEmployee.getJoinDate() != null &&
            !updateDTO.getProbationEndDate().isAfter(existingEmployee.getJoinDate())) {
            throw new BusinessException("试用期结束日期必须晚于入职日期");
        }

        // 8. 转换DTO到Entity
        Employee employee = new Employee();
        BeanUtils.copyProperties(updateDTO, employee);

        // 9. 更新版本号
        employee.setVersion(existingEmployee.getVersion() + 1);

        // 10. 更新员工
        employeeMapper.updateById(employee);

        // 11. 记录操作日志
        saveOperationLog(employee.getId(), "更新员工", "更新员工信息");

        log.info("更新员工成功: {}", employee.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployeeStatus(String id, EmployeeStatusDTO statusDTO) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null || employee.getIsDeleted()) {
            throw new BusinessException("员工不存在");
        }

        // 更新状态
        employee.setStatus(EmployeeStatus.valueOf(statusDTO.getStatus().toUpperCase()));

        // 如果是离职,同时更新试用期状态
        if (employee.getStatus() == EmployeeStatus.RESIGNED) {
            employee.setProbationStatus(ProbationStatus.RESIGNED);
        }

        employeeMapper.updateById(employee);

        // 记录操作日志
        saveOperationLog(id, "更新状态", "状态变更为: " + employee.getStatus().getDescription());

        log.info("更新员工状态成功: {} -> {}", id, employee.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(String id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null || employee.getIsDeleted()) {
            throw new BusinessException("员工不存在");
        }

        // 1. 检查是否有下属员工
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getManagerId, id);
        Long count = employeeMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该员工有下属,无法删除");
        }

        // 2. 逻辑删除
        employeeMapper.deleteById(id);

        // 3. 记录操作日志
        saveOperationLog(id, "删除员工", "删除员工: " + employee.getName());

        log.info("删除员工成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteEmployees(List<String> ids) {
        for (String id : ids) {
            deleteEmployee(id);
        }
    }

    @Override
    public EmployeeStatisticsVO getStatistics() {
        EmployeeStatisticsVO statistics = new EmployeeStatisticsVO();

        // 总员工数
        LambdaQueryWrapper<Employee> allWrapper = new LambdaQueryWrapper<>();
        statistics.setTotal(employeeMapper.selectCount(allWrapper));

        // 在职人数
        LambdaQueryWrapper<Employee> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Employee::getStatus, EmployeeStatus.ACTIVE);
        statistics.setActive(employeeMapper.selectCount(activeWrapper));

        // 离职人数
        LambdaQueryWrapper<Employee> resignedWrapper = new LambdaQueryWrapper<>();
        resignedWrapper.eq(Employee::getStatus, EmployeeStatus.RESIGNED);
        statistics.setResigned(employeeMapper.selectCount(resignedWrapper));

        // 停薪留职人数
        LambdaQueryWrapper<Employee> suspendedWrapper = new LambdaQueryWrapper<>();
        suspendedWrapper.eq(Employee::getStatus, EmployeeStatus.SUSPENDED);
        statistics.setSuspended(employeeMapper.selectCount(suspendedWrapper));

        // 试用期人数
        LambdaQueryWrapper<Employee> probationWrapper = new LambdaQueryWrapper<>();
        probationWrapper.eq(Employee::getProbationStatus, ProbationStatus.PROBATION);
        statistics.setProbation(employeeMapper.selectCount(probationWrapper));

        // 本月新入职
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LambdaQueryWrapper<Employee> newWrapper = new LambdaQueryWrapper<>();
        newWrapper.ge(Employee::getJoinDate, monthStart);
        statistics.setNewThisMonth(employeeMapper.selectCount(newWrapper));

        return statistics;
    }

    @Override
    public ImportResultVO importEmployees(MultipartFile file) {
        // TODO: 实现Excel导入逻辑
        return new ImportResultVO();
    }

    @Override
    public void exportEmployees(EmployeeQueryDTO queryDTO, HttpServletResponse response) {
        // TODO: 实现Excel导出逻辑
    }

    /**
     * 计算工龄
     */
    private Integer calculateWorkYears(LocalDate joinDate) {
        if (joinDate == null) {
            return 0;
        }

        LocalDate now = LocalDate.now();
        Period period = Period.between(joinDate, now);
        return Math.max(0, period.getYears());
    }

    /**
     * 记录操作日志
     */
    private void saveOperationLog(String employeeId, String operation, String details) {
        EmployeeOperationLog log = new EmployeeOperationLog();
        log.setEmployeeId(employeeId);
        log.setOperation(operation);
        log.setOperator(SecurityUtils.getUserId());
        log.setOperatorName(SecurityUtils.getUsername());
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        log.setIpAddress(SecurityUtils.getIpAddress());
        log.setUserAgent(SecurityUtils.getUserAgent());

        operationLogMapper.insert(log);
    }

    /**
     * 转换为VO列表
     */
    private List<EmployeeVO> convertToVOList(List<Employee> employees) {
        List<EmployeeVO> voList = new ArrayList<>();
        for (Employee employee : employees) {
            EmployeeVO vo = new EmployeeVO();
            BeanUtils.copyProperties(employee, vo);

            // 查询部门名称
            Department department = departmentMapper.selectById(employee.getDepartmentId());
            if (department != null) {
                vo.setDepartmentName(department.getName());
            }

            // 查询上级姓名
            if (StringUtils.hasText(employee.getManagerId())) {
                Employee manager = employeeMapper.selectById(employee.getManagerId());
                if (manager != null) {
                    vo.setManagerName(manager.getName());
                }
            }

            voList.add(vo);
        }
        return voList;
    }
}
```

---

## 5. Controller接口设计

### 5.1 EmployeeController

```java
package com.oa.system.core.employee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.system.common.result.Result;
import com.oa.system.core.employee.dto.*;
import com.oa.system.core.employee.service.EmployeeService;
import com.oa.system.core.employee.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 员工管理Controller
 */
@Tag(name = "员工管理", description = "员工管理接口")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    @Operation(summary = "获取员工详情")
    @PreAuthorize("hasAuthority('employee:view')")
    public Result<EmployeeDetailVO> getEmployee(@PathVariable String id) {
        EmployeeDetailVO employee = employeeService.getEmployeeDetail(id);
        return Result.success(employee);
    }

    @GetMapping
    @Operation(summary = "分页查询员工列表")
    @PreAuthorize("hasAuthority('employee:view')")
    public Result<Page<EmployeeVO>> pageEmployee(@Valid EmployeeQueryDTO queryDTO) {
        Page<EmployeeVO> page = employeeService.pageEmployee(queryDTO);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建员工")
    @PreAuthorize("hasAuthority('employee:create')")
    public Result<String> createEmployee(@Valid @RequestBody EmployeeCreateDTO createDTO) {
        String id = employeeService.createEmployee(createDTO);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新员工信息")
    @PreAuthorize("hasAuthority('employee:edit')")
    public Result<Void> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody EmployeeUpdateDTO updateDTO
    ) {
        updateDTO.setId(id);
        employeeService.updateEmployee(updateDTO);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新员工状态")
    @PreAuthorize("hasAuthority('employee:edit')")
    public Result<Void> updateEmployeeStatus(
            @PathVariable String id,
            @Valid @RequestBody EmployeeStatusDTO statusDTO
    ) {
        employeeService.updateEmployeeStatus(id, statusDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工")
    @PreAuthorize("hasAuthority('employee:delete')")
    public Result<Void> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除员工")
    @PreAuthorize("hasAuthority('employee:delete')")
    public Result<Void> batchDeleteEmployees(@RequestBody List<String> ids) {
        employeeService.batchDeleteEmployees(ids);
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取员工统计数据")
    @PreAuthorize("hasAuthority('employee:view')")
    public Result<EmployeeStatisticsVO> getStatistics() {
        EmployeeStatisticsVO statistics = employeeService.getStatistics();
        return Result.success(statistics);
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入员工")
    @PreAuthorize("hasAuthority('employee:import')")
    public Result<ImportResultVO> importEmployees(@RequestParam("file") MultipartFile file) {
        ImportResultVO result = employeeService.importEmployees(file);
        return Result.success(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出员工列表")
    @PreAuthorize("hasAuthority('employee:export')")
    public void exportEmployees(EmployeeQueryDTO queryDTO, HttpServletResponse response) {
        employeeService.exportEmployees(queryDTO, response);
    }
}
```

---

## 6. DTO数据传输对象

由于篇幅限制,DTO定义将在后续文档中详细展开。

---

## 7. 异常处理

### 7.1 业务异常定义

```java
package com.oa.system.core.employee.exception;

import com.oa.system.common.exception.BusinessException;

/**
 * 员工业务异常
 */
public class EmployeeException extends BusinessException {

    public static final EmployeeException EMPLOYEE_NOT_FOUND =
            new EmployeeException(1001, "员工不存在");

    public static final EmployeeException EMPLOYEE_EMAIL_EXISTS =
            new EmployeeException(1002, "邮箱已被使用");

    public static final EmployeeException EMPLOYEE_PHONE_EXISTS =
            new EmployeeException(1003, "手机号已被使用");

    public static final EmployeeException EMPLOYEE_HAS_SUBORDINATES =
            new EmployeeException(1004, "该员工有下属,无法删除");

    public static final EmployeeException INVALID_JOIN_DATE =
            new EmployeeException(1005, "入职日期不能晚于今天");

    public EmployeeException(int code, String message) {
        super(code, message);
    }
}
```

---

## 8. 定时任务

### 8.1 员工定时任务配置

```java
package com.oa.system.core.employee.schedule;

import com.oa.system.core.employee.entity.Employee;
import com.oa.system.core.employee.mapper.EmployeeMapper;
import com.oa.system.core.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeSchedule {

    private final EmployeeMapper employeeMapper;
    private final EmployeeService employeeService;

    /**
     * 生日提醒
     * 每天早上9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkBirthdayReminders() {
        log.info("开始检查员工生日提醒...");

        List<Employee> birthdayEmployees = employeeMapper.selectByBirthDate(LocalDate.now());

        for (Employee employee : birthdayEmployees) {
            // TODO: 发送生日祝福通知
            log.info("员工 {} 今天生日", employee.getName());
        }

        log.info("生日提醒检查完成,共 {} 人", birthdayEmployees.size());
    }

    /**
     * 转正提醒
     * 每天早上9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkProbationReminders() {
        log.info("开始检查员工转正提醒...");

        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        List<Employee> expiringEmployees = employeeMapper.selectByProbationEndDateBetween(
                today, in7Days
        );

        for (Employee employee : expiringEmployees) {
            // TODO: 发送转正提醒通知
            log.info("员工 {} 试用期即将到期", employee.getName());
        }

        log.info("转正提醒检查完成,共 {} 人", expiringEmployees.size());
    }

    /**
     * 工龄自动更新
     * 每月1号凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void updateWorkYears() {
        log.info("开始更新员工工龄...");

        List<Employee> employees = employeeMapper.selectList(null);

        for (Employee employee : employees) {
            if (employee.getJoinDate() != null) {
                int newWorkYears = calculateWorkYears(employee.getJoinDate());

                if (!newWorkYears.equals(employee.getWorkYears())) {
                    employee.setWorkYears(newWorkYears);
                    employeeMapper.updateById(employee);
                    log.info("员工 {} 工龄更新为 {} 年", employee.getName(), newWorkYears);
                }
            }
        }

        log.info("工龄更新完成");
    }

    private int calculateWorkYears(LocalDate joinDate) {
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

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
