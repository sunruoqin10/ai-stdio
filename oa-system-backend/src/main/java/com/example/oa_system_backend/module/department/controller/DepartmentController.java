package com.example.oa_system_backend.module.department.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.department.dto.*;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.service.DepartmentService;
import com.example.oa_system_backend.module.department.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 *
 * @author OA System
 * @since 2026-01-15
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 分页查询部门列表
     * GET /api/departments
     */
    @GetMapping
    public ApiResponse<IPage<DepartmentVO>> getDepartmentList(DepartmentQueryRequest request) {
        IPage<DepartmentVO> result = departmentService.getDepartmentList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取部门树形结构
     * GET /api/departments/tree
     */
    @GetMapping("/tree")
    public ApiResponse<List<DepartmentVO>> getDepartmentTree() {
        List<DepartmentVO> tree = departmentService.getDepartmentTree();
        return ApiResponse.success(tree);
    }

    /**
     * 获取根部门列表
     * GET /api/departments/roots
     */
    @GetMapping("/roots")
    public ApiResponse<List<DepartmentVO>> getRootDepartments() {
        List<DepartmentVO> roots = departmentService.getRootDepartments();
        return ApiResponse.success(roots);
    }

    /**
     * 获取子部门列表
     * GET /api/departments/{parentId}/children
     */
    @GetMapping("/{parentId}/children")
    public ApiResponse<List<DepartmentVO>> getChildDepartments(@PathVariable String parentId) {
        List<DepartmentVO> children = departmentService.getChildDepartments(parentId);
        return ApiResponse.success(children);
    }

    /**
     * 获取部门详情
     * GET /api/departments/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<DepartmentDetailVO> getDepartmentById(@PathVariable String id) {
        DepartmentDetailVO department = departmentService.getDepartmentById(id);
        return ApiResponse.success(department);
    }

    /**
     * 创建部门
     * POST /api/departments
     */
    @PostMapping
    public ApiResponse<Department> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request) {
        Department department = departmentService.createDepartment(request);
        return ApiResponse.success("创建成功", department);
    }

    /**
     * 更新部门信息
     * PUT /api/departments/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<Department> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        Department department = departmentService.updateDepartment(id, request);
        return ApiResponse.success("更新成功", department);
    }

    /**
     * 移动部门
     * PUT /api/departments/{id}/move
     */
    @PutMapping("/{id}/move")
    public ApiResponse<Void> moveDepartment(
            @PathVariable String id,
            @Valid @RequestBody DepartmentMoveRequest request) {
        departmentService.moveDepartment(id, request);
        return ApiResponse.success("移动成功", null);
    }

    /**
     * 删除部门
     * DELETE /api/departments/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable String id) {
        departmentService.deleteDepartment(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 批量删除部门
     * DELETE /api/departments/batch
     */
    @DeleteMapping("/batch")
    public ApiResponse<BatchResultVO> batchDeleteDepartments(
            @RequestBody List<String> ids) {
        BatchResultVO result = departmentService.batchDeleteDepartments(ids);
        return ApiResponse.success("批量删除完成", result);
    }

    /**
     * 获取部门成员列表
     * GET /api/departments/{id}/members
     */
    @GetMapping("/{id}/members")
    public ApiResponse<List<DepartmentMemberVO>> getDepartmentMembers(@PathVariable String id) {
        List<DepartmentMemberVO> members = departmentService.getDepartmentMembers(id);
        return ApiResponse.success(members);
    }

    /**
     * 获取部门统计信息
     * GET /api/departments/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<DepartmentStatisticsVO> getStatistics() {
        DepartmentStatisticsVO statistics = departmentService.getStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 检查部门名称是否存在
     * GET /api/departments/check-name
     */
    @GetMapping("/check-name")
    public ApiResponse<Boolean> checkNameExists(
            @RequestParam String name,
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) String excludeId) {
        boolean exists = departmentService.checkNameExists(name, parentId, excludeId);
        return ApiResponse.success(exists);
    }

    /**
     * 检查部门是否有子部门
     * GET /api/departments/{id}/has-children
     */
    @GetMapping("/{id}/has-children")
    public ApiResponse<Boolean> hasChildren(@PathVariable String id) {
        boolean hasChildren = departmentService.hasChildren(id);
        return ApiResponse.success(hasChildren);
    }

    /**
     * 检查部门是否有成员
     * GET /api/departments/{id}/has-members
     */
    @GetMapping("/{id}/has-members")
    public ApiResponse<Boolean> hasMembers(@PathVariable String id) {
        boolean hasMembers = departmentService.hasMembers(id);
        return ApiResponse.success(hasMembers);
    }
}
