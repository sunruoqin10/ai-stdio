package com.example.oa_system_backend.module.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.permission.dto.AssignRoleRequest;
import com.example.oa_system_backend.module.permission.entity.Role;
import com.example.oa_system_backend.module.permission.entity.UserRole;
import com.example.oa_system_backend.module.permission.mapper.RoleMapper;
import com.example.oa_system_backend.module.permission.mapper.UserRoleMapper;
import com.example.oa_system_backend.module.permission.service.UserRoleService;
import com.example.oa_system_backend.module.permission.vo.RoleVO;
import com.example.oa_system_backend.module.permission.vo.UserPermissionsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleVO> getUserRoles(String userId) {
        log.info("获取用户角色, userId={}", userId);
        
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getIsDeleted, 0);
        
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        
        return userRoles.stream().map(userRole -> {
            Role role = roleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                RoleVO vo = new RoleVO();
                BeanUtils.copyProperties(role, vo);
                return vo;
            }
            return null;
        }).filter(vo -> vo != null).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignUserRoles(String userId, AssignRoleRequest request) {
        log.info("分配用户角色, userId={}, roleIds={}", userId, request.getRoleIds());
        
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getIsDeleted, 0);
        
        List<UserRole> existingUserRoles = userRoleMapper.selectList(wrapper);
        
        for (UserRole userRole : existingUserRoles) {
            if (!request.getRoleIds().contains(userRole.getRoleId())) {
                userRole.setIsDeleted(1);
                userRole.setDeletedAt(LocalDateTime.now());
                userRoleMapper.updateById(userRole);
            }
        }
        
        for (String roleId : request.getRoleIds()) {
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                throw new BusinessException("角色不存在: " + roleId);
            }
            
            boolean exists = existingUserRoles.stream()
                    .anyMatch(ur -> ur.getRoleId().equals(roleId));
            
            if (!exists) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString().replace("-", ""));
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreatedAt(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional
    public void removeUserRole(String userId, String roleId) {
        log.info("移除用户角色, userId={}, roleId={}", userId, roleId);
        
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getRoleId, roleId);
        wrapper.eq(UserRole::getIsDeleted, 0);
        
        UserRole userRole = userRoleMapper.selectOne(wrapper);
        if (userRole == null) {
            throw new BusinessException("用户角色关系不存在");
        }
        
        userRole.setIsDeleted(1);
        userRole.setDeletedAt(LocalDateTime.now());
        userRoleMapper.updateById(userRole);
    }

    @Override
    public UserPermissionsVO getUserPermissions(String userId) {
        log.info("获取用户权限, userId={}", userId);
        
        List<RoleVO> roles = getUserRoles(userId);
        
        UserPermissionsVO userPermissions = new UserPermissionsVO();
        userPermissions.setUserId(userId);
        userPermissions.setRoles(roles);
        
        return userPermissions;
    }
}