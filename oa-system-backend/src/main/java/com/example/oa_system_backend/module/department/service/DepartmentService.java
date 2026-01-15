package com.example.oa_system_backend.module.department.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.department.dto.*;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.vo.*;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author OA System
 * @since 2026-01-15
 */
public interface DepartmentService {

    /**
     * 分页查询部门列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<DepartmentVO> getDepartmentList(DepartmentQueryRequest request);

    /**
     * 获取部门树形结构
     *
     * @return 部门树列表
     */
    List<DepartmentVO> getDepartmentTree();

    /**
     * 获取根部门列表
     *
     * @return 根部门列表
     */
    List<DepartmentVO> getRootDepartments();

    /**
     * 获取子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<DepartmentVO> getChildDepartments(String parentId);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    DepartmentDetailVO getDepartmentById(String id);

    /**
     * 创建部门
     *
     * @param request 创建请求
     * @return 创建的部门
     */
    Department createDepartment(DepartmentCreateRequest request);

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param request 更新请求
     * @return 更新后的部门
     */
    Department updateDepartment(String id, DepartmentUpdateRequest request);

    /**
     * 移动部门（更改父部门）
     *
     * @param id 部门ID
     * @param request 移动请求
     */
    void moveDepartment(String id, DepartmentMoveRequest request);

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
     * 获取部门成员列表
     *
     * @param departmentId 部门ID
     * @return 成员列表
     */
    List<DepartmentMemberVO> getDepartmentMembers(String departmentId);

    /**
     * 获取部门统计信息
     *
     * @return 统计信息
     */
    DepartmentStatisticsVO getStatistics();

    /**
     * 检查部门名称是否存在
     *
     * @param name 部门名称
     * @param parentId 父部门ID
     * @param excludeId 排除的部门ID
     * @return 是否存在
     */
    boolean checkNameExists(String name, String parentId, String excludeId);

    /**
     * 检查部门是否有子部门
     *
     * @param id 部门ID
     * @return 是否有子部门
     */
    boolean hasChildren(String id);

    /**
     * 检查部门是否有成员
     *
     * @param id 部门ID
     * @return 是否有成员
     */
    boolean hasMembers(String id);

    /**
     * 更新部门成员数量
     *
     * @param departmentId 部门ID
     */
    void updateEmployeeCount(String departmentId);

    /**
     * 更新部门子部门数量
     *
     * @param departmentId 部门ID
     */
    void updateChildCount(String departmentId);

    /**
     * 验证部门是否可以删除
     *
     * @param id 部门ID
     * @throws RuntimeException 如果不能删除
     */
    void validateDelete(String id);
}
