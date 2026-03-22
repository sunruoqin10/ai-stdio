package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserPermissionsVO {
    private String userId;
    private List<RoleVO> roles;
    private List<PermissionVO> permissions;
    private List<String> permissionCodes;
    private List<String> buttonPermissions;
    private List<String> apiPermissions;
    private List<PermissionVO> menuPermissions;
    private Map<String, String> dataPermissions;
}