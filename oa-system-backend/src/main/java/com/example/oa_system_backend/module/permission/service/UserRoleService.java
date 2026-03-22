package com.example.oa_system_backend.module.permission.service;

import com.example.oa_system_backend.module.permission.dto.AssignRoleRequest;
import com.example.oa_system_backend.module.permission.vo.RoleVO;
import com.example.oa_system_backend.module.permission.vo.UserPermissionsVO;

import java.util.List;

public interface UserRoleService {

    List<RoleVO> getUserRoles(String userId);

    void assignUserRoles(String userId, AssignRoleRequest request);

    void removeUserRole(String userId, String roleId);

    UserPermissionsVO getUserPermissions(String userId);
}