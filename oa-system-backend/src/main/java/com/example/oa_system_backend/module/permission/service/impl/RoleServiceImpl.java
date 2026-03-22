package com.example.oa_system_backend.module.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.permission.dto.*;
import com.example.oa_system_backend.module.permission.entity.Role;
import com.example.oa_system_backend.module.permission.entity.UserRole;
import com.example.oa_system_backend.module.permission.mapper.RoleMapper;
import com.example.oa_system_backend.module.permission.mapper.UserRoleMapper;
import com.example.oa_system_backend.module.permission.service.RoleService;
import com.example.oa_system_backend.module.permission.vo.*;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public IPage<RoleVO> getRoleList(RoleQueryRequest request) {
        log.info("查询角色列表, keyword={}, status={}, type={}, page={}, pageSize={}",
                request.getKeyword(), request.getStatus(), request.getType(),
                request.getPage(), request.getPageSize());
        
        Page<RoleVO> page = new Page<>(request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Role::getName, request.getKeyword())
                    .or()
                    .like(Role::getCode, request.getKeyword()));
        }
        if (request.getStatus() != null) {
            wrapper.eq(Role::getStatus, request.getStatus());
        }
        if (request.getType() != null) {
            wrapper.eq(Role::getType, request.getType());
        }
        wrapper.orderByAsc(Role::getSort);
        
        Page<Role> rolePage = roleMapper.selectPage(new Page<>(request.getPage(), request.getPageSize()), wrapper);
        
        List<RoleVO> voList = rolePage.getRecords().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
        
        Page<RoleVO> result = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        result.setRecords(voList);
        
        return result;
    }

    @Override
    public RoleDetailVO getRoleById(String id) {
        log.info("获取角色详情, id={}", id);
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        RoleDetailVO detail = new RoleDetailVO();
        BeanUtils.copyProperties(role, detail);
        
        return detail;
    }

    @Override
    @Transactional
    public Role createRole(RoleCreateRequest request) {
        log.info("创建角色, name={}", request.getName());
        
        if (checkCodeExists(request.getCode(), null)) {
            throw new BusinessException("角色编码已存在");
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        role.setId(generateRoleId());
        role.setIsPreset(false);
        role.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        
        roleMapper.insert(role);
        
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(String id, RoleUpdateRequest request) {
        log.info("更新角色, id={}", id);
        
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        if (role.getIsPreset()) {
            throw new BusinessException("系统预设角色不允许修改");
        }
        
        if (request.getCode() != null && checkCodeExists(request.getCode(), id)) {
            throw new BusinessException("角色编码已存在");
        }
        
        BeanUtils.copyProperties(request, role);
        role.setUpdatedAt(LocalDateTime.now());
        
        roleMapper.updateById(role);
        
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(String id) {
        log.info("删除角色, id={}", id);
        
        validateDelete(id);
        
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Role copyRole(CopyRoleRequest request) {
        log.info("复制角色, sourceId={}, newName={}", request.getSourceId(), request.getName());
        
        Role sourceRole = roleMapper.selectById(request.getSourceId());
        if (sourceRole == null) {
            throw new BusinessException("源角色不存在");
        }
        
        Role newRole = new Role();
        BeanUtils.copyProperties(sourceRole, newRole);
        newRole.setId(generateRoleId());
        newRole.setName(request.getName());
        newRole.setCode("COPY_" + sourceRole.getCode() + "_" + System.currentTimeMillis());
        newRole.setType("custom");
        newRole.setIsPreset(false);
        newRole.setCreatedAt(LocalDateTime.now());
        newRole.setUpdatedAt(LocalDateTime.now());
        
        roleMapper.insert(newRole);
        
        return newRole;
    }

    @Override
    public List<String> getRolePermissions(String id) {
        log.info("获取角色权限, roleId={}", id);
        
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        return List.of();
    }

    @Override
    @Transactional
    public void updateRolePermissions(String id, UpdateRolePermissionsRequest request) {
        log.info("更新角色权限, roleId={}, permissionIds={}", id, request.getPermissionIds());
        
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
    }

    @Override
    public List<UserVO> getRoleUsers(String id) {
        log.info("获取角色成员, roleId={}", id);
        
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        return List.of();
    }

    @Override
    public RoleStatisticsVO getStatistics() {
        log.info("获取角色统计信息");
        
        List<Role> allRoles = roleMapper.selectList(new LambdaQueryWrapper<>());
        
        RoleStatisticsVO statistics = new RoleStatisticsVO();
        statistics.setTotal(allRoles.size());
        statistics.setSystemCount((int) allRoles.stream().filter(r -> "system".equals(r.getType())).count());
        statistics.setCustomCount((int) allRoles.stream().filter(r -> "custom".equals(r.getType())).count());
        statistics.setActiveCount((int) allRoles.stream().filter(r -> "active".equals(r.getStatus())).count());
        
        return statistics;
    }

    @Override
    public boolean checkCodeExists(String code, String excludeId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        return roleMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasUsers(String id) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getRoleId, id);
        return userRoleMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void validateDelete(String id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        if (role.getIsPreset()) {
            throw new BusinessException("系统预设角色不允许删除");
        }
        
        if (hasUsers(id)) {
            throw new BusinessException("该角色下还有用户，无法删除");
        }
    }

    @Override
    public String generateRoleId() {
        return "ROLE" + String.format("%04d", (int) (Math.random() * 10000));
    }
}