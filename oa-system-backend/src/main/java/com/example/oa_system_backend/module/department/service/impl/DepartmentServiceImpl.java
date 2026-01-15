package com.example.oa_system_backend.module.department.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.department.dto.*;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.mapper.DepartmentMapper;
import com.example.oa_system_backend.module.department.service.DepartmentService;
import com.example.oa_system_backend.module.department.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public IPage<DepartmentVO> getDepartmentList(DepartmentQueryRequest request) {
        Page<DepartmentVO> page = new Page<>(request.getPage(), request.getPageSize());
        return departmentMapper.selectPageByQuery(page, request);
    }

    @Override
    @Cacheable(value = "department:list", key = "'tree'")
    public List<DepartmentVO> getDepartmentTree() {
        log.info("获取部门树形结构");
        List<DepartmentVO> allDepartments = departmentMapper.selectDepartmentTree(null);

        // 构建树形结构
        return buildTree(allDepartments, null);
    }

    @Override
    @Cacheable(value = "department:list", key = "'roots'")
    public List<DepartmentVO> getRootDepartments() {
        log.info("获取根部门列表");
        return departmentMapper.selectRootDepartments(null);
    }

    @Override
    @Cacheable(value = "department:children", key = "#parentId")
    public List<DepartmentVO> getChildDepartments(String parentId) {
        log.info("获取子部门列表, parentId={}", parentId);
        return departmentMapper.selectChildDepartments(parentId, null);
    }

    @Override
    @Cacheable(value = "department:detail", key = "#id")
    public DepartmentDetailVO getDepartmentById(String id) {
        log.info("获取部门详情, id={}", id);
        Department department = departmentMapper.selectDepartmentDetail(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 转换为DepartmentDetailVO
        DepartmentDetailVO detail = new DepartmentDetailVO();
        BeanUtils.copyProperties(department, detail);
        return detail;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:statistics"}, allEntries = true)
    public Department createDepartment(DepartmentCreateRequest request) {
        log.info("创建部门, name={}", request.getName());

        // 1. 验证部门名称在同级唯一
        if (checkNameExists(request.getName(), request.getParentId(), null)) {
            throw new BusinessException("同级部门名称已存在");
        }

        // 2. 验证父部门存在（如果指定）
        if (request.getParentId() != null) {
            Department parent = departmentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
        }

        // 3. 构建部门实体
        Department department = new Department();
        BeanUtils.copyProperties(request, department);

        // 4. 计算部门层级
        if (request.getParentId() == null) {
            department.setLevel(1);
        } else {
            Department parent = departmentMapper.selectById(request.getParentId());
            department.setLevel(parent.getLevel() + 1);
        }

        // 5. 设置默认值
        department.setStatus("active");
        department.setEmployeeCount(0);
        department.setChildCount(0);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        // 6. 保存到数据库
        departmentMapper.insert(department);

        // 7. 更新父部门的子部门数量
        if (department.getParentId() != null) {
            updateChildCount(department.getParentId());
        }

        log.info("部门创建成功, id={}, name={}", department.getId(), department.getName());
        return department;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:statistics"}, allEntries = true)
    public Department updateDepartment(String id, DepartmentUpdateRequest request) {
        log.info("更新部门信息, id={}", id);

        // 1. 检查部门是否存在
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 2. 验证部门名称在同级唯一（如果更新了名称）
        if (request.getName() != null && !request.getName().equals(department.getName())) {
            if (checkNameExists(request.getName(), department.getParentId(), id)) {
                throw new BusinessException("同级部门名称已存在");
            }
        }

        // 3. 更新字段
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getShortName() != null) {
            department.setShortName(request.getShortName());
        }
        if (request.getLeaderId() != null) {
            department.setLeaderId(request.getLeaderId());
        }
        if (request.getEstablishedDate() != null) {
            department.setEstablishedDate(request.getEstablishedDate());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            department.setIcon(request.getIcon());
        }
        if (request.getSort() != null) {
            department.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            department.setStatus(request.getStatus());
        }

        department.setUpdatedAt(LocalDateTime.now());

        // 4. 保存更新（乐观锁）
        int rows = departmentMapper.updateById(department);
        if (rows == 0) {
            throw new BusinessException("部门信息已被其他用户修改，请刷新后重试");
        }

        log.info("部门信息更新成功, id={}", id);
        return department;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:statistics"}, allEntries = true)
    public void moveDepartment(String id, DepartmentMoveRequest request) {
        log.info("移动部门, id={}, newParentId={}", id, request.getNewParentId());

        // 1. 检查部门是否存在
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 2. 不能移动到自己的子孙节点下
        List<String> descendantIds = departmentMapper.selectDescendantIds(id);
        if (descendantIds.contains(request.getNewParentId())) {
            throw new BusinessException("不能将部门移动到其子部门下");
        }

        // 3. 验证新父部门存在
        Department newParent = departmentMapper.selectById(request.getNewParentId());
        if (newParent == null) {
            throw new BusinessException("目标父部门不存在");
        }

        // 4. 验证部门名称在新父部门下唯一
        if (checkNameExists(department.getName(), request.getNewParentId(), id)) {
            throw new BusinessException("目标父部门下已存在同名部门");
        }

        // 5. 记录旧父部门ID
        String oldParentId = department.getParentId();

        // 6. 更新父部门和层级
        department.setParentId(request.getNewParentId());
        department.setLevel(newParent.getLevel() + 1);
        department.setUpdatedAt(LocalDateTime.now());

        int rows = departmentMapper.updateById(department);
        if (rows == 0) {
            throw new BusinessException("部门信息已被其他用户修改，请刷新后重试");
        }

        // 7. 更新旧父部门的子部门数量
        if (oldParentId != null) {
            updateChildCount(oldParentId);
        }

        // 8. 更新新父部门的子部门数量
        updateChildCount(request.getNewParentId());

        log.info("部门移动成功, id={}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:statistics"}, allEntries = true)
    public void deleteDepartment(String id) {
        log.info("删除部门, id={}", id);

        // 1. 验证是否可以删除
        validateDelete(id);

        // 2. 逻辑删除
        departmentMapper.deleteById(id);

        // 3. 更新父部门的子部门数量
        Department department = departmentMapper.selectById(id);
        if (department != null && department.getParentId() != null) {
            updateChildCount(department.getParentId());
        }

        log.info("部门删除成功, id={}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"department:list", "department:detail", "department:children", "department:statistics"}, allEntries = true)
    public BatchResultVO batchDeleteDepartments(List<String> ids) {
        log.info("批量删除部门, ids={}", ids);

        BatchResultVO result = new BatchResultVO();
        result.setTotal(ids.size());
        result.setSuccess(0);
        result.setFailed(0);
        result.setErrors(new ArrayList<>());

        for (String id : ids) {
            try {
                deleteDepartment(id);
                result.setSuccess(result.getSuccess() + 1);
            } catch (Exception e) {
                log.error("删除部门失败, id={}, error={}", id, e.getMessage());
                result.setFailed(result.getFailed() + 1);

                BatchResultVO.BatchError error = new BatchResultVO.BatchError();
                error.setId(id);
                error.setMessage(e.getMessage());
                result.getErrors().add(error);
            }
        }

        log.info("批量删除完成, total={}, success={}, failed={}",
                result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    @Override
    @Cacheable(value = "department:members", key = "#departmentId")
    public List<DepartmentMemberVO> getDepartmentMembers(String departmentId) {
        log.info("获取部门成员, departmentId={}", departmentId);
        return departmentMapper.selectDepartmentMembers(departmentId);
    }

    @Override
    @Cacheable(value = "department:statistics", key = "'overview'")
    public DepartmentStatisticsVO getStatistics() {
        log.info("获取部门统计信息");
        return departmentMapper.selectStatistics();
    }

    @Override
    public boolean checkNameExists(String name, String parentId, String excludeId) {
        Long count = departmentMapper.countByNameInSameLevel(name, parentId, excludeId);
        return count != null && count > 0;
    }

    @Override
    public boolean hasChildren(String id) {
        Long count = departmentMapper.countChildren(id);
        return count != null && count > 0;
    }

    @Override
    public boolean hasMembers(String id) {
        Long count = departmentMapper.countEmployees(id);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void updateEmployeeCount(String departmentId) {
        Long count = departmentMapper.countEmployees(departmentId);
        Department department = departmentMapper.selectById(departmentId);
        if (department != null) {
            department.setEmployeeCount(count != null ? count.intValue() : 0);
            department.setUpdatedAt(LocalDateTime.now());
            departmentMapper.updateById(department);
        }
    }

    @Override
    @Transactional
    public void updateChildCount(String departmentId) {
        Long count = departmentMapper.countChildren(departmentId);
        Department department = departmentMapper.selectById(departmentId);
        if (department != null) {
            department.setChildCount(count != null ? count.intValue() : 0);
            department.setUpdatedAt(LocalDateTime.now());
            departmentMapper.updateById(department);
        }
    }

    @Override
    public void validateDelete(String id) {
        // 1. 检查部门是否存在
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 2. 检查是否有子部门
        if (hasChildren(id)) {
            throw new BusinessException("部门下存在子部门，无法删除");
        }

        // 3. 检查是否有成员
        if (hasMembers(id)) {
            throw new BusinessException("部门下存在成员，无法删除");
        }
    }

    /**
     * 构建部门树
     *
     * @param departments 所有部门列表
     * @param parentId 父部门ID
     * @return 树形结构
     */
    private List<DepartmentVO> buildTree(List<DepartmentVO> departments, String parentId) {
        return departments.stream()
                .filter(dept -> {
                    if (parentId == null) {
                        return dept.getParentId() == null || dept.getParentId().isEmpty();
                    }
                    return parentId.equals(dept.getParentId());
                })
                .peek(dept -> {
                    List<DepartmentVO> children = buildTree(departments, dept.getId());
                    dept.setChildren(children);
                })
                .collect(Collectors.toList());
    }
}
