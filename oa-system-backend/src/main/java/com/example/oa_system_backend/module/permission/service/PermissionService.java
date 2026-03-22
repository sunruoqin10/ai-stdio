package com.example.oa_system_backend.module.permission.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.permission.dto.*;
import com.example.oa_system_backend.module.permission.entity.Permission;
import com.example.oa_system_backend.module.permission.vo.*;

import java.util.List;

public interface PermissionService {

    List<PermissionVO> getPermissionTree(PermissionQueryRequest request);

    IPage<PermissionVO> getPermissionList(PermissionQueryRequest request);

    PermissionDetailVO getPermissionById(String id);

    Permission createPermission(PermissionCreateRequest request);

    Permission updatePermission(String id, PermissionUpdateRequest request);

    void deletePermission(String id);

    List<String> getPermissionModules();

    PermissionStatisticsVO getStatistics();

    boolean checkCodeExists(String code, String excludeId);

    boolean hasChildren(String id);

    void validateDelete(String id);

    String generatePermissionId();
}