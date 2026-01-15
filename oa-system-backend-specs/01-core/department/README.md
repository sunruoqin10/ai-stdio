# 部门管理模块 - 后端技术规范

> **模块名称**: department
> **技术栈**: Spring Boot 3.x + MyBatis-Plus 3.5.x + MySQL 8.0+
> **版本**: v1.0.0
> **创建日期**: 2026-01-15

---

## 📋 文档目录

1. [模块概述](#模块概述)
2. [技术架构](#技术架构)
3. [数据模型设计](#数据模型设计)
4. [Controller层规范](#controller层规范)
5. [Service层规范](#service层规范)
6. [Mapper层规范](#mapper层规范)
7. [业务逻辑与约束](#业务逻辑与约束)
8. [缓存策略](#缓存策略)
9. [性能优化](#性能优化)
10. [测试规范](#测试规范)

---

## 模块概述

### 功能范围

部门管理模块提供完整的组织架构管理功能,包括:

- ✅ 部门CRUD操作
- ✅ 树形结构管理
- ✅ 部门层级管理(最多5级)
- ✅ 部门移动与调整
- ✅ 部门成员管理
- ✅ 部门统计与分析
- ✅ 数据导入导出
- ✅ 批量操作支持

### 核心特性

1. **层级管理**: 支持最多5级部门层级,自动计算部门层级
2. **树形结构**: 支持树形和扁平两种数据展示方式
3. **关联管理**: 与员工模块深度集成,支持部门负责人设置
4. **数据完整性**: 完整的外键约束和业务规则校验
5. **性能优化**: 采用缓存策略和查询优化提升性能
6. **可扩展性**: 预留扩展字段,支持未来功能扩展

### 技术亮点

- **MyBatis-Plus**: 利用MyBatis-Plus的Tree机制快速实现树形结构
- **Redis缓存**: 使用Redis缓存部门树和统计数据
- **乐观锁**: 使用version字段实现乐观锁,防止并发冲突
- **软删除**: 采用逻辑删除,保证数据可追溯
- **审计日志**: 完整的创建人、更新人、删除人记录

---

## 技术架构

### 分层架构

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← RESTful API接口层
│  DepartmentController               │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          Service Layer              │  ← 业务逻辑层
│  DepartmentService                  │
│  DepartmentQueryService             │
│  DepartmentValidateService          │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│           Mapper Layer              │  ← 数据访问层
│  DepartmentMapper                   │
│  DepartmentMemberMapper             │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          Database Layer             │  ← 数据存储层
│  MySQL 8.0+                         │
└─────────────────────────────────────┘
```

### 核心依赖

```xml
<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Boot Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- MyBatis-Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>

<!-- Caffeine Cache (本地高性能缓存) -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Apache POI (Excel导出) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

---

## 数据模型设计

### 实体类设计

#### Department (部门实体)

**文件路径**: `com/oa/system/module/department/entity/Department.java`

```java
package com.oa.system.module.department.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 部门实体类
 *
 * @author OA System
 * @since 2026-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_department")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID (格式: DEPT + 4位序号)
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门简称
     */
    private String shortName;

    /**
     * 上级部门ID
     */
    private String parentId;

    /**
     * 部门负责人ID
     */
    private String leaderId;

    /**
     * 部门层级 (1-5)
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 成立时间
     */
    private LocalDate establishedDate;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 部门图标URL
     */
    private String icon;

    /**
     * 状态: active-正常, disabled-停用
     */
    private String status;

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

    /**
     * 是否删除 (0-否, 1-是)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    private String deletedBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    // ==================== 非数据库字段 ====================

    /**
     * 部门负责人姓名 (关联查询)
     */
    @TableField(exist = false)
    private String leaderName;

    /**
     * 上级部门名称 (关联查询)
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 员工数量 (统计查询)
     */
    @TableField(exist = false)
    private Integer employeeCount;

    /**
     * 是否有子部门 (树形查询)
     */
    @TableField(exist = false)
    private Boolean hasChildren;
}
```

#### DepartmentMember (部门成员关系实体)

**文件路径**: `com/oa/system/module/department/entity/DepartmentMember.java`

```java
package com.oa.system.module.department.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 部门成员关系实体
 *
 * @author OA System
 * @since 2026-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_department_member")
public class DepartmentMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 员工ID
     */
    private String employeeId;

    /**
     * 是否为负责人 (0-否, 1-是)
     */
    private Integer isLeader;

    /**
     * 加入部门日期
     */
    private LocalDate joinDate;

    /**
     * 离开部门日期
     */
    private LocalDate leaveDate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

---

## Controller层规范

### DepartmentController

**文件路径**: `com/oa/system/module/department/controller/DepartmentController.java`

```java
package com.oa.system.module.department.controller;

import com.oa.system.common.result.Result;
import com.oa.system.module.department.dto.*;
import com.oa.system.module.department.service.DepartmentService;
import com.oa.system.module.department.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 部门管理接口
 *
 * @author OA System
 * @since 2026-01-15
 */
@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门列表
     */
    @Operation(summary = "获取部门列表", description = "支持树形和扁平两种展示方式")
    @GetMapping
    public Result<DepartmentListVO> getDepartmentList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaderId,
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        DepartmentQueryDTO queryDTO = DepartmentQueryDTO.builder()
                .type(type)
                .keyword(keyword)
                .status(status)
                .leaderId(leaderId)
                .level(level)
                .page(page)
                .pageSize(pageSize)
                .build();

        DepartmentListVO result = departmentService.getDepartmentList(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取部门详情
     */
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
    @GetMapping("/{id}")
    public Result<DepartmentDetailVO> getDepartmentDetail(
            @PathVariable String id
    ) {
        DepartmentDetailVO detail = departmentService.getDepartmentDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取子部门列表
     */
    @Operation(summary = "获取子部门列表", description = "获取指定部门的所有直接子部门")
    @GetMapping("/{id}/children")
    public Result<List<DepartmentVO>> getChildren(
            @PathVariable String id
    ) {
        List<DepartmentVO> children = departmentService.getChildren(id);
        return Result.success(children);
    }

    /**
     * 获取部门成员列表
     */
    @Operation(summary = "获取部门成员列表", description = "获取指定部门的所有成员")
    @GetMapping("/{id}/employees")
    public Result<List<DepartmentMemberVO>> getDepartmentMembers(
            @PathVariable String id
    ) {
        List<DepartmentMemberVO> members = departmentService.getDepartmentMembers(id);
        return Result.success(members);
    }

    /**
     * 创建部门
     */
    @Operation(summary = "创建部门", description = "创建新的部门")
    @PostMapping
    public Result<String> createDepartment(
            @Valid @RequestBody DepartmentCreateDTO createDTO
    ) {
        String departmentId = departmentService.createDepartment(createDTO);
        return Result.success(departmentId);
    }

    /**
     * 更新部门
     */
    @Operation(summary = "更新部门", description = "更新部门信息")
    @PutMapping("/{id}")
    public Result<Void> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody DepartmentUpdateDTO updateDTO
    ) {
        departmentService.updateDepartment(id, updateDTO);
        return Result.success();
    }

    /**
     * 移动部门
     */
    @Operation(summary = "移动部门", description = "移动部门到新的父部门下")
    @PutMapping("/{id}/move")
    public Result<Void> moveDepartment(
            @PathVariable String id,
            @Valid @RequestBody DepartmentMoveDTO moveDTO
    ) {
        departmentService.moveDepartment(id, moveDTO);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门", description = "根据ID删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(
            @PathVariable String id
    ) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    /**
     * 批量删除部门
     */
    @Operation(summary = "批量删除部门", description = "批量删除多个部门")
    @DeleteMapping("/batch")
    public Result<BatchResultVO> batchDeleteDepartments(
            @RequestBody @NotEmpty(message = "删除列表不能为空") List<String> ids
    ) {
        BatchResultVO result = departmentService.batchDeleteDepartments(ids);
        return Result.success(result);
    }

    /**
     * 获取部门统计信息
     */
    @Operation(summary = "获取部门统计信息", description = "获取部门统计数据")
    @GetMapping("/statistics")
    public Result<DepartmentStatisticsVO> getStatistics() {
        DepartmentStatisticsVO statistics = departmentService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 导出部门列表
     */
    @Operation(summary = "导出部门列表", description = "导出部门列表为Excel文件")
    @PostMapping("/export")
    public void exportDepartments(
            @RequestBody(required = false) DepartmentQueryDTO queryDTO,
            HttpServletResponse response
    ) throws IOException {
        departmentService.exportDepartments(queryDTO, response);
    }
}
```

---

**完整规范文档请查看:**

1. [API接口详细规范](./api-specification.md)
2. [Service层业务逻辑](./service-specification.md)
3. [Mapper层数据访问](./mapper-specification.md)
4. [业务规则与约束](./business-rules.md)
5. [数据验证规则](./validation-rules.md)

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-15
