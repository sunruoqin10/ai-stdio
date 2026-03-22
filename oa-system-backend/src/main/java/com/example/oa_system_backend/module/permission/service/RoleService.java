package com.example.oa_system_backend.module.permission.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.permission.dto.*;
import com.example.oa_system_backend.module.permission.entity.Role;
import com.example.oa_system_backend.module.permission.vo.*;

import java.util.List;

public interface RoleService {

    IPage<RoleVO> getRoleList(RoleQueryRequest request);

    RoleDetailVO getRoleById(String id);

    Role createRole(RoleCreateRequest request);

    Role updateRole(String id, RoleUpdateRequest request);

    void deleteRole(String id);

    Role copyRole(CopyRoleRequest request);

    List<String> getRolePermissions(String id);

    void updateRolePermissions(String id, UpdateRolePermissionsRequest request);

    List<UserVO> getRoleUsers(String id);

    RoleStatisticsVO getStatistics();

    boolean checkCodeExists(String code, String excludeId);

    boolean hasUsers(String id);

    void validateDelete(String id);

    String generateRoleId();
}