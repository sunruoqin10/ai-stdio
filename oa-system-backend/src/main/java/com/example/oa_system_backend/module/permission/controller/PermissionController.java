package com.example.oa_system_backend.module.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.permission.dto.*;
import com.example.oa_system_backend.module.permission.entity.Permission;
import com.example.oa_system_backend.module.permission.service.PermissionService;
import com.example.oa_system_backend.module.permission.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    public ApiResponse<List<PermissionVO>> getPermissionTree(PermissionQueryRequest request) {
        log.info("获取权限树, keyword={}, type={}, module={}", 
                request.getKeyword(), request.getType(), request.getModule());
        List<PermissionVO> tree = permissionService.getPermissionTree(request);
        return ApiResponse.success(tree);
    }

    @GetMapping
    public ApiResponse<IPage<PermissionVO>> getPermissionList(PermissionQueryRequest request) {
        log.info("获取权限列表, keyword={}, type={}, module={}", 
                request.getKeyword(), request.getType(), request.getModule());
        IPage<PermissionVO> result = permissionService.getPermissionList(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionDetailVO> getPermissionById(@PathVariable String id) {
        log.info("获取权限详情, id={}", id);
        PermissionDetailVO permission = permissionService.getPermissionById(id);
        return ApiResponse.success(permission);
    }

    @PostMapping
    public ApiResponse<Permission> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        log.info("创建权限, name={}", request.getName());
        Permission permission = permissionService.createPermission(request);
        return ApiResponse.success("创建成功", permission);
    }

    @PutMapping("/{id}")
    public ApiResponse<Permission> updatePermission(@PathVariable String id, 
                                                     @Valid @RequestBody PermissionUpdateRequest request) {
        log.info("更新权限, id={}", id);
        Permission permission = permissionService.updatePermission(id, request);
        return ApiResponse.success("更新成功", permission);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable String id) {
        log.info("删除权限, id={}", id);
        permissionService.deletePermission(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/modules")
    public ApiResponse<List<String>> getPermissionModules() {
        log.info("获取所有模块");
        List<String> modules = permissionService.getPermissionModules();
        return ApiResponse.success(modules);
    }

    @GetMapping("/statistics")
    public ApiResponse<PermissionStatisticsVO> getStatistics() {
        log.info("获取权限统计信息");
        PermissionStatisticsVO statistics = permissionService.getStatistics();
        return ApiResponse.success(statistics);
    }
}