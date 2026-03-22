package com.example.oa_system_backend.module.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.permission.dto.*;
import com.example.oa_system_backend.module.permission.entity.Role;
import com.example.oa_system_backend.module.permission.service.RoleService;
import com.example.oa_system_backend.module.permission.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<IPage<RoleVO>> getRoleList(RoleQueryRequest request) {
        log.info("获取角色列表, keyword={}, status={}, type={}", 
                request.getKeyword(), request.getStatus(), request.getType());
        IPage<RoleVO> result = roleService.getRoleList(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleDetailVO> getRoleById(@PathVariable String id) {
        log.info("获取角色详情, id={}", id);
        RoleDetailVO role = roleService.getRoleById(id);
        return ApiResponse.success(role);
    }

    @PostMapping
    public ApiResponse<Role> createRole(@Valid @RequestBody RoleCreateRequest request) {
        log.info("创建角色, name={}", request.getName());
        Role role = roleService.createRole(request);
        return ApiResponse.success("创建成功", role);
    }

    @PutMapping("/{id}")
    public ApiResponse<Role> updateRole(@PathVariable String id, 
                                         @Valid @RequestBody RoleUpdateRequest request) {
        log.info("更新角色, id={}", id);
        Role role = roleService.updateRole(id, request);
        return ApiResponse.success("更新成功", role);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable String id) {
        log.info("删除角色, id={}", id);
        roleService.deleteRole(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/copy")
    public ApiResponse<Role> copyRole(@Valid @RequestBody CopyRoleRequest request) {
        log.info("复制角色, sourceId={}, name={}", request.getSourceId(), request.getName());
        Role role = roleService.copyRole(request);
        return ApiResponse.success("复制成功", role);
    }

    @GetMapping("/statistics")
    public ApiResponse<RoleStatisticsVO> getStatistics() {
        log.info("获取角色统计信息");
        RoleStatisticsVO statistics = roleService.getStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<String>> getRolePermissions(@PathVariable String id) {
        log.info("获取角色权限, roleId={}", id);
        List<String> permissions = roleService.getRolePermissions(id);
        return ApiResponse.success(permissions);
    }

    @PutMapping("/{id}/permissions")
    public ApiResponse<Void> updateRolePermissions(@PathVariable String id, 
                                                      @Valid @RequestBody UpdateRolePermissionsRequest request) {
        log.info("更新角色权限, roleId={}, permissionIds={}", id, request.getPermissionIds());
        roleService.updateRolePermissions(id, request);
        return ApiResponse.success("更新成功", null);
    }

    @GetMapping("/{id}/users")
    public ApiResponse<List<UserVO>> getRoleUsers(@PathVariable String id) {
        log.info("获取角色成员, roleId={}", id);
        List<UserVO> users = roleService.getRoleUsers(id);
        return ApiResponse.success(users);
    }
}