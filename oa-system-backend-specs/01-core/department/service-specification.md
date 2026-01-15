# 部门管理 Service 层规范

> **模块**: department
> **版本**: v1.0.0
> **更新日期**: 2026-01-15

---

## 🎯 Service 层架构

### 服务分层

```
DepartmentService (主服务)
    ├── DepartmentQueryService (查询服务)
    ├── DepartmentValidateService (验证服务)
    └── DepartmentTreeService (树形结构服务)
```

---

## 📦 核心服务类

### 1. DepartmentService (主服务)

**文件路径**: `com/oa/system/module/department/service/DepartmentService.java`

```java
package com.oa.system.module.department.service;

import com.oa.system.module.department.dto.*;
import com.oa.system.module.department.entity.Department;
import com.oa.system.module.department.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 部门服务接口
 *
 * @author OA System
 * @since 2026-01-15
 */
public interface DepartmentService {

    /**
     * 获取部门列表
     *
     * @param queryDTO 查询条件
     * @return 部门列表
     */
    DepartmentListVO getDepartmentList(DepartmentQueryDTO queryDTO);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    DepartmentDetailVO getDepartmentDetail(String id);

    /**
     * 获取子部门列表
     *
     * @param id 父部门ID
     * @return 子部门列表
     */
    List<DepartmentVO> getChildren(String id);

    /**
     * 获取部门成员列表
     *
     * @param id 部门ID
     * @return 部门成员列表
     */
    List<DepartmentMemberVO> getDepartmentMembers(String id);

    /**
     * 创建部门
     *
     * @param createDTO 创建信息
     * @return 部门ID
     */
    String createDepartment(DepartmentCreateDTO createDTO);

    /**
     * 更新部门
     *
     * @param id 部门ID
     * @param updateDTO 更新信息
     */
    void updateDepartment(String id, DepartmentUpdateDTO updateDTO);

    /**
     * 移动部门
     *
     * @param id 部门ID
     * @param moveDTO 移动信息
     */
    void moveDepartment(String id, DepartmentMoveDTO moveDTO);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDepartment(String id);

    /**
     * 批量删除部门
     *
     * @param ids 部门ID列表
     * @return 批量操作结果
     */
    BatchResultVO batchDeleteDepartments(List<String> ids);

    /**
     * 获取部门统计信息
     *
     * @return 统计信息
     */
    DepartmentStatisticsVO getStatistics();

    /**
     * 导出部门列表
     *
     * @param queryDTO 查询条件
     * @param response HTTP响应
     */
    void exportDepartments(DepartmentQueryDTO queryDTO, HttpServletResponse response) throws IOException;
}
```

---

### 2. DepartmentServiceImpl (实现类)

**文件路径**: `com/oa/system/module/department/service/impl/DepartmentServiceImpl.java`

```java
package com.oa.system.module.department.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.system.common.exception.BusinessException;
import com.oa.system.module.department.dto.*;
import com.oa.system.module.department.entity.Department;
import com.oa.system.module.department.entity.DepartmentMember;
import com.oa.system.module.department.mapper.DepartmentMapper;
import com.oa.system.module.department.mapper.DepartmentMemberMapper;
import com.oa.system.module.department.service.DepartmentService;
import com.oa.system.module.department.service.DepartmentQueryService;
import com.oa.system.module.department.service.DepartmentValidateService;
import com.oa.system.module.department.service.DepartmentTreeService;
import com.oa.system.module.department.util.DepartmentIdGenerator;
import com.oa.system.module.department.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 *
 * @author OA System
 * @since 2026-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl
        extends ServiceImpl<DepartmentMapper, Department>
        implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final DepartmentMemberMapper departmentMemberMapper;
    private final DepartmentQueryService queryService;
    private final DepartmentValidateService validateService;
    private final DepartmentTreeService treeService;
    private final DepartmentIdGenerator idGenerator;

    @Override
    @Cacheable(value = "department:list", key = "#queryDTO.toString()")
    public DepartmentListVO getDepartmentList(DepartmentQueryDTO queryDTO) {
        log.info("查询部门列表,查询条件: {}", queryDTO);

        // 判断查询类型
        if ("tree".equals(queryDTO.getType())) {
            // 树形查询
            List<DepartmentVO> tree = queryService.getTree(queryDTO);
            return DepartmentListVO.builder()
                    .list(tree)
                    .total((long) tree.size())
                    .build();
        } else {
            // 扁平查询
            return queryService.getFlatList(queryDTO);
        }
    }

    @Override
    @Cacheable(value = "department:detail", key = "#id")
    public DepartmentDetailVO getDepartmentDetail(String id) {
        log.info("获取部门详情,部门ID: {}", id);

        // 查询部门
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(2001, "部门不存在");
        }

        // 查询子部门数量
        Integer childCount = departmentMapper.selectCount(
                Wrappers.<Department>lambdaQuery()
                        .eq(Department::getParentId, id)
        );

        // 转换为VO
        DepartmentDetailVO detailVO = BeanUtil.copyProperties(department, DepartmentDetailVO.class);

        // 查询负责人信息
        if (department.getLeaderId() != null) {
            // TODO: 调用员工服务获取负责人信息
            // Employee leader = employeeService.getById(department.getLeaderId());
            // detailVO.setLeaderName(leader.getName());
            // detailVO.setLeaderPosition(leader.getPosition());
        }

        // 查询父部门名称
        if (department.getParentId() != null) {
            Department parent = departmentMapper.selectById(department.getParentId());
            if (parent != null) {
                detailVO.setParentName(parent.getName());
            }
        }

        // 设置子部门数量
        detailVO.setChildCount(childCount);

        return detailVO;
    }

    @Override
    @Cacheable(value = "department:children", key = "#id")
    public List<DepartmentVO> getChildren(String id) {
        log.info("获取子部门列表,父部门ID: {}", id);

        // 验证父部门存在
        Department parent = departmentMapper.selectById(id);
        if (parent == null) {
            throw new BusinessException(2001, "部门不存在");
        }

        // 查询子部门
        List<Department> children = departmentMapper.selectList(
                Wrappers.<Department>lambdaQuery()
                        .eq(Department::getParentId, id)
                        .orderByAsc(Department::getSort)
        );

        // 转换为VO
        return children.stream()
                .map(dept -> {
                    DepartmentVO vo = BeanUtil.copyProperties(dept, DepartmentVO.class);
                    // 设置员工数量
                    Integer employeeCount = departmentMemberMapper.selectCount(
                            Wrappers.<DepartmentMember>lambdaQuery()
                                    .eq(DepartmentMember::getDepartmentId, dept.getId())
                                    .isNull(DepartmentMember::getLeaveDate)
                    );
                    vo.setEmployeeCount(employeeCount);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "department:members", key = "#id")
    public List<DepartmentMemberVO> getDepartmentMembers(String id) {
        log.info("获取部门成员列表,部门ID: {}", id);

        // 验证部门存在
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(2001, "部门不存在");
        }

        // 查询部门成员
        List<DepartmentMember> members = departmentMemberMapper.selectList(
                Wrappers.<DepartmentMember>lambdaQuery()
                        .eq(DepartmentMember::getDepartmentId, id)
                        .isNull(DepartmentMember::getLeaveDate)
        );

        // 转换为VO
        return members.stream()
                .map(member -> {
                    DepartmentMemberVO vo = new DepartmentMemberVO();
                    vo.setEmployeeId(member.getEmployeeId());
                    vo.setIsLeader(member.getIsLeader() == 1);
                    vo.setJoinDepartmentDate(member.getJoinDate());

                    // TODO: 调用员工服务获取员工信息
                    // Employee employee = employeeService.getById(member.getEmployeeId());
                    // vo.setEmployeeName(employee.getName());
                    // vo.setEmployeeAvatar(employee.getAvatar());
                    // vo.setPosition(employee.getPosition());
                    // vo.setStatus(employee.getStatus());

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:members"}, allEntries = true)
    public String createDepartment(DepartmentCreateDTO createDTO) {
        log.info("创建部门,创建信息: {}", createDTO);

        // 1. 验证部门名称唯一性
        validateService.validateNameUnique(createDTO.getName(), createDTO.getParentId());

        // 2. 验证上级部门存在
        if (createDTO.getParentId() != null) {
            validateService.validateParentExists(createDTO.getParentId());
        }

        // 3. 验证负责人存在
        validateService.validateLeaderExists(createDTO.getLeaderId());

        // 4. 计算部门层级
        Integer level = calculateLevel(createDTO.getParentId());

        // 5. 验证层级不超过5级
        if (level > 5) {
            throw new BusinessException(2003, "部门层级不能超过5级");
        }

        // 6. 创建部门实体
        Department department = new Department();
        department.setId(idGenerator.generate());
        department.setName(createDTO.getName());
        department.setShortName(createDTO.getShortName());
        department.setParentId(createDTO.getParentId());
        department.setLeaderId(createDTO.getLeaderId());
        department.setLevel(level);
        department.setSort(createDTO.getSort() != null ? createDTO.getSort() : 0);
        department.setEstablishedDate(createDTO.getEstablishedDate());
        department.setDescription(createDTO.getDescription());
        department.setIcon(createDTO.getIcon());
        department.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : "active");
        department.setVersion(0);

        // 7. 保存部门
        departmentMapper.insert(department);

        // 8. 如果设置了负责人,更新部门成员关系
        // TODO: 更新部门成员关系表

        log.info("部门创建成功,部门ID: {}", department.getId());
        return department.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:members"}, allEntries = true)
    public void updateDepartment(String id, DepartmentUpdateDTO updateDTO) {
        log.info("更新部门,部门ID: {}, 更新信息: {}", id, updateDTO);

        // 1. 查询部门
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(2001, "部门不存在");
        }

        // 2. 验证乐观锁版本
        if (!department.getVersion().equals(updateDTO.getVersion())) {
            throw new BusinessException(2008, "数据已被其他用户修改,请刷新后重试");
        }

        // 3. 验证部门名称唯一性(如果修改了名称)
        if (updateDTO.getName() != null && !updateDTO.getName().equals(department.getName())) {
            validateService.validateNameUnique(updateDTO.getName(), department.getParentId());
        }

        // 4. 验证负责人存在(如果修改了负责人)
        if (updateDTO.getLeaderId() != null && !updateDTO.getLeaderId().equals(department.getLeaderId())) {
            validateService.validateLeaderExists(updateDTO.getLeaderId());
        }

        // 5. 更新部门信息
        if (updateDTO.getName() != null) {
            department.setName(updateDTO.getName());
        }
        if (updateDTO.getShortName() != null) {
            department.setShortName(updateDTO.getShortName());
        }
        if (updateDTO.getLeaderId() != null) {
            department.setLeaderId(updateDTO.getLeaderId());
        }
        if (updateDTO.getSort() != null) {
            department.setSort(updateDTO.getSort());
        }
        if (updateDTO.getEstablishedDate() != null) {
            department.setEstablishedDate(updateDTO.getEstablishedDate());
        }
        if (updateDTO.getDescription() != null) {
            department.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getIcon() != null) {
            department.setIcon(updateDTO.getIcon());
        }
        if (updateDTO.getStatus() != null) {
            department.setStatus(updateDTO.getStatus());
        }

        // 6. 版本号+1
        department.setVersion(department.getVersion() + 1);

        // 7. 更新部门
        int rows = departmentMapper.updateById(department);
        if (rows == 0) {
            throw new BusinessException(2008, "更新失败,数据已被其他用户修改");
        }

        log.info("部门更新成功,部门ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:members"}, allEntries = true)
    public void moveDepartment(String id, DepartmentMoveDTO moveDTO) {
        log.info("移动部门,部门ID: {}, 移动信息: {}", id, moveDTO);

        // 1. 查询部门
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(2001, "部门不存在");
        }

        // 2. 验证乐观锁版本
        if (!department.getVersion().equals(moveDTO.getVersion())) {
            throw new BusinessException(2008, "数据已被其他用户修改,请刷新后重试");
        }

        // 3. 验证新父部门
        String newParentId = moveDTO.getNewParentId();
        if (newParentId != null) {
            // 3.1 不能移动到自己
            if (id.equals(newParentId)) {
                throw new BusinessException(2004, "不能将部门设置为自己的父部门");
            }

            // 3.2 不能移动到自己的子部门
            validateService.validateNotMoveToChild(id, newParentId);

            // 3.3 验证新父部门存在
            validateService.validateParentExists(newParentId);

            // 3.4 计算新层级
            Department newParent = departmentMapper.selectById(newParentId);
            Integer newLevel = newParent.getLevel() + 1;

            // 3.5 验证层级不超过5级
            if (newLevel > 5) {
                throw new BusinessException(2003, "移动后层级不能超过5级");
            }
        }

        // 4. 移动部门(更新父部门和层级)
        treeService.moveDepartmentTree(id, newParentId);

        log.info("部门移动成功,部门ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:members"}, allEntries = true)
    public void deleteDepartment(String id) {
        log.info("删除部门,部门ID: {}", id);

        // 1. 查询部门
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(2001, "部门不存在");
        }

        // 2. 验证没有子部门
        validateService.validateNoChildren(id);

        // 3. 验证没有员工
        validateService.validateNoEmployees(id);

        // 4. 软删除部门
        department.setIsDeleted(1);
        department.setDeletedAt(LocalDateTime.now());
        departmentMapper.updateById(department);

        log.info("部门删除成功,部门ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:members"}, allEntries = true)
    public BatchResultVO batchDeleteDepartments(List<String> ids) {
        log.info("批量删除部门,部门IDs: {}", ids);

        BatchResultVO result = new BatchResultVO();
        result.setTotal(ids.size());
        result.setSuccess(0);
        result.setFailed(0);
        result.setErrors(new ArrayList<>());

        for (String id : ids) {
            try {
                deleteDepartment(id);
                result.setSuccess(result.getSuccess() + 1);
            } catch (BusinessException e) {
                result.setFailed(result.getFailed() + 1);
                BatchError error = new BatchError();
                error.setId(id);
                error.setMessage(e.getMessage());
                result.getErrors().add(error);
            } catch (Exception e) {
                log.error("删除部门失败,部门ID: {}", id, e);
                result.setFailed(result.getFailed() + 1);
                BatchError error = new BatchError();
                error.setId(id);
                error.setMessage("删除失败: " + e.getMessage());
                result.getErrors().add(error);
            }
        }

        log.info("批量删除完成,总数: {}, 成功: {}, 失败: {}",
                result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    @Override
    @Cacheable(value = "department:statistics")
    public DepartmentStatisticsVO getStatistics() {
        log.info("获取部门统计信息");

        // 查询统计信息
        return departmentMapper.selectStatistics();
    }

    @Override
    public void exportDepartments(DepartmentQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        log.info("导出部门列表,查询条件: {}", queryDTO);

        // TODO: 实现Excel导出逻辑
        // 1. 查询部门列表
        // 2. 使用Apache POI生成Excel文件
        // 3. 写入响应流

        log.info("部门列表导出成功");
    }

    /**
     * 计算部门层级
     *
     * @param parentId 父部门ID
     * @return 层级
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
}
```

---

### 3. DepartmentQueryService (查询服务)

**文件路径**: `com/oa/system/module/department/service/DepartmentQueryService.java`

```java
package com.oa.system.module.department.service;

import com.oa.system.module.department.dto.DepartmentQueryDTO;
import com.oa.system.module.department.vo.DepartmentListVO;
import com.oa.system.module.department.vo.DepartmentVO;

import java.util.List;

/**
 * 部门查询服务
 *
 * @author OA System
 * @since 2026-01-15
 */
public interface DepartmentQueryService {

    /**
     * 获取树形部门列表
     *
     * @param queryDTO 查询条件
     * @return 树形部门列表
     */
    List<DepartmentVO> getTree(DepartmentQueryDTO queryDTO);

    /**
     * 获取扁平部门列表
     *
     * @param queryDTO 查询条件
     * @return 扁平部门列表
     */
    DepartmentListVO getFlatList(DepartmentQueryDTO queryDTO);
}
```

---

### 4. DepartmentValidateService (验证服务)

**文件路径**: `com/oa/system/module/department/service/DepartmentValidateService.java`

```java
package com.oa.system.module.department.service;

import com.oa.system.common.exception.BusinessException;

/**
 * 部门验证服务
 *
 * @author OA System
 * @since 2026-01-15
 */
public interface DepartmentValidateService {

    /**
     * 验证部门名称唯一性
     *
     * @param name 部门名称
     * @param parentId 父部门ID
     */
    void validateNameUnique(String name, String parentId);

    /**
     * 验证父部门存在
     *
     * @param parentId 父部门ID
     */
    void validateParentExists(String parentId);

    /**
     * 验证负责人存在
     *
     * @param leaderId 负责人ID
     */
    void validateLeaderExists(String leaderId);

    /**
     * 验证没有子部门
     *
     * @param departmentId 部门ID
     */
    void validateNoChildren(String departmentId);

    /**
     * 验证没有员工
     *
     * @param departmentId 部门ID
     */
    void validateNoEmployees(String departmentId);

    /**
     * 验证不能移动到子部门
     *
     * @param departmentId 部门ID
     * @param newParentId 新父部门ID
     */
    void validateNotMoveToChild(String departmentId, String newParentId);
}
```

---

### 5. DepartmentTreeService (树形结构服务)

**文件路径**: `com/oa/system/module/department/service/DepartmentTreeService.java`

```java
package com.oa.system.module.department.service;

/**
 * 部门树形结构服务
 *
 * @author OA System
 * @since 2026-01-15
 */
public interface DepartmentTreeService {

    /**
     * 移动部门树(更新层级)
     *
     * @param departmentId 部门ID
     * @param newParentId 新父部门ID
     */
    void moveDepartmentTree(String departmentId, String newParentId);

    /**
     * 获取部门的所有子孙部门ID
     *
     * @param departmentId 部门ID
     * @return 子孙部门ID列表
     */
    List<String> getAllDescendantIds(String departmentId);

    /**
     * 构建部门树
     *
     * @param departments 扁平部门列表
     * @return 树形部门列表
     */
    List<DepartmentVO> buildTree(List<Department> departments);
}
```

---

## 🔧 辅助工具类

### DepartmentIdGenerator (ID生成器)

**文件路径**: `com/oa/system/module/department/util/DepartmentIdGenerator.java`

```java
package com.oa.system.module.department.util;

import org.springframework.stereotype.Component;

/**
 * 部门ID生成器
 * 格式: DEPT + 4位序号
 *
 * @author OA System
 * @since 2026-01-15
 */
@Component
public class DepartmentIdGenerator {

    /**
     * 生成部门ID
     *
     * @return 部门ID
     */
    public String generate() {
        // TODO: 实现ID生成逻辑
        // 1. 查询数据库中最大的部门ID
        // 2. 序号+1
        // 3. 格式化为 DEPT + 4位序号

        return "DEPT" + String.format("%04d", 1);
    }
}
```

---

## 📊 缓存策略

### Caffeine缓存配置

使用Caffeine作为本地缓存,提供高性能的内存缓存。

#### 缓存配置类

**文件路径**: `com/oa/system/config/CacheConfig.java`

```java
package com.oa.system.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存配置
 *
 * @author OA System
 * @since 2026-01-15
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 部门列表缓存 - 5分钟过期
        cacheManager.addCaches(buildCache("department:list", 5));

        // 部门详情缓存 - 10分钟过期
        cacheManager.addCaches(buildCache("department:detail", 10));

        // 子部门列表缓存 - 5分钟过期
        cacheManager.addCaches(buildCache("department:children", 5));

        // 部门成员缓存 - 5分钟过期
        cacheManager.addCaches(buildCache("department:members", 5));

        // 统计信息缓存 - 1分钟过期
        cacheManager.addCaches(buildCache("department:statistics", 1));

        return cacheManager;
    }

    /**
     * 构建Caffeine缓存
     *
     * @param cacheName 缓存名称
     * @param ttlMinutes 过期时间(分钟)
     * @return CaffeineCache
     */
    private CaffeineCache buildCache(String cacheName, int ttlMinutes) {
        return new CaffeineCache(cacheName,
            Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(50)
                // 最大容量
                .maximumSize(500)
                // 写入后过期时间
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                // 开启统计
                .recordStats()
                .build()
        );
    }
}
```

#### 缓存使用示例

```java
// 查询时使用缓存
@Cacheable(value = "department:detail", key = "#id")
public DepartmentDetailVO getDepartmentDetail(String id) {
    // ...
}

// 更新时清除缓存
@CacheEvict(value = {"department:list", "department:detail",
                    "department:children", "department:members"},
              allEntries = true)
public void updateDepartment(String id, DepartmentUpdateDTO updateDTO) {
    // ...
}
```

### 缓存策略说明

| 缓存名称 | 缓存内容 | TTL | 最大容量 | 失效策略 |
|---------|---------|-----|---------|---------|
| department:list | 部门列表 | 5分钟 | 500 | 增删改时清除 |
| department:detail | 部门详情 | 10分钟 | 500 | 增删改时清除 |
| department:children | 子部门列表 | 5分钟 | 500 | 增删改时清除 |
| department:members | 部门成员 | 5分钟 | 500 | 增删改时清除 |
| department:statistics | 统计信息 | 1分钟 | 100 | 定时刷新 |

### 缓存监控

```java
// 获取缓存统计信息
CaffeineCache cache = (CaffeineCache) cacheManager.getCache("department:list");
CacheStats stats = cache.getNativeCache().stats();

// 统计指标
- hitRate(): 缓存命中率
- hitCount(): 命中次数
- missCount(): 未命中次数
- evictionCount(): 驱逐次数
```

---

**文档版本**: v1.0.1
**创建人**: AI开发助手
**最后更新**: 2026-01-15
**更新内容**: 使用Caffeine替代Redis作为缓存方案
