package com.example.oa_system_backend.module.permission.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.permission.dto.AssignRoleRequest;
import com.example.oa_system_backend.module.permission.service.UserRoleService;
import com.example.oa_system_backend.module.permission.vo.RoleVO;
import com.example.oa_system_backend.module.permission.vo.UserPermissionsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/{userId}/roles")
    public ApiResponse<List<RoleVO>> getUserRoles(@PathVariable String userId) {
        log.info("获取用户角色, userId={}", userId);
        List<RoleVO> roles = userRoleService.getUserRoles(userId);
        return ApiResponse.success(roles);
    }

    @PostMapping("/{userId}/roles")
    public ApiResponse<Void> assignUserRoles(@PathVariable String userId, 
                                                @Valid @RequestBody AssignRoleRequest request) {
        log.info("分配用户角色, userId={}, roleIds={}", userId, request.getRoleIds());
        userRoleService.assignUserRoles(userId, request);
        return ApiResponse.success("分配成功", null);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<Void> removeUserRole(@PathVariable String userId, 
                                               @PathVariable String roleId) {
        log.info("移除用户角色, userId={}, roleId={}", userId, roleId);
        userRoleService.removeUserRole(userId, roleId);
        return ApiResponse.success("移除成功", null);
    }

    @GetMapping("/{userId}/permissions")
    public ApiResponse<UserPermissionsVO> getUserPermissions(@PathVariable String userId) {
        log.info("获取用户权限, userId={}", userId);
        UserPermissionsVO permissions = userRoleService.getUserPermissions(userId);
        return ApiResponse.success(permissions);
    }
}