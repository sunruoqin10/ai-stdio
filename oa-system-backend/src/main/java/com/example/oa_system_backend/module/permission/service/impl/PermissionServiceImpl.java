package com.example.oa_system_backend.module.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.permission.dto.*;
import com.example.oa_system_backend.module.permission.entity.Permission;
import com.example.oa_system_backend.module.permission.mapper.PermissionMapper;
import com.example.oa_system_backend.module.permission.service.PermissionService;
import com.example.oa_system_backend.module.permission.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionVO> getPermissionTree(PermissionQueryRequest request) {
        log.info("获取权限树, keyword={}, type={}, module={}",
                request.getKeyword(), request.getType(), request.getModule());
        
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Permission::getName, request.getKeyword())
                    .or()
                    .like(Permission::getCode, request.getKeyword()));
        }
        if (request.getType() != null) {
            wrapper.eq(Permission::getType, request.getType());
        }
        if (request.getModule() != null) {
            wrapper.eq(Permission::getModule, request.getModule());
        }
        wrapper.eq(Permission::getStatus, "active");
        wrapper.orderByAsc(Permission::getSort);
        
        List<Permission> allPermissions = permissionMapper.selectList(wrapper);
        
        List<PermissionVO> voList = allPermissions.stream().map(perm -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(perm, vo);
            return vo;
        }).collect(Collectors.toList());
        
        return buildTree(voList, null);
    }

    @Override
    public IPage<PermissionVO> getPermissionList(PermissionQueryRequest request) {
        log.info("获取权限列表, keyword={}, type={}, module={}, page={}, pageSize={}",
                request.getKeyword(), request.getType(), request.getModule(),
                request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Permission::getName, request.getKeyword())
                    .or()
                    .like(Permission::getCode, request.getKeyword()));
        }
        if (request.getType() != null) {
            wrapper.eq(Permission::getType, request.getType());
        }
        if (request.getModule() != null) {
            wrapper.eq(Permission::getModule, request.getModule());
        }
        wrapper.orderByAsc(Permission::getSort);
        
        Page<Permission> permPage = permissionMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);
        
        List<PermissionVO> voList = permPage.getRecords().stream().map(perm -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(perm, vo);
            return vo;
        }).collect(Collectors.toList());
        
        Page<PermissionVO> result = new Page<>(permPage.getCurrent(), permPage.getSize(), permPage.getTotal());
        result.setRecords(voList);
        
        return result;
    }

    @Override
    public PermissionDetailVO getPermissionById(String id) {
        log.info("获取权限详情, id={}", id);
        
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        
        PermissionDetailVO detail = new PermissionDetailVO();
        BeanUtils.copyProperties(permission, detail);
        
        return detail;
    }

    @Override
    @Transactional
    public Permission createPermission(PermissionCreateRequest request) {
        log.info("创建权限, name={}", request.getName());
        
        if (checkCodeExists(request.getCode(), null)) {
            throw new BusinessException("权限编码已存在");
        }
        
        Permission permission = new Permission();
        BeanUtils.copyProperties(request, permission);
        permission.setId(generatePermissionId());
        permission.setStatus("active");
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        
        permissionMapper.insert(permission);
        
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(String id, PermissionUpdateRequest request) {
        log.info("更新权限, id={}", id);
        
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        
        if (request.getCode() != null && checkCodeExists(request.getCode(), id)) {
            throw new BusinessException("权限编码已存在");
        }
        
        BeanUtils.copyProperties(request, permission);
        permission.setUpdatedAt(LocalDateTime.now());
        
        permissionMapper.updateById(permission);
        
        return permission;
    }

    @Override
    @Transactional
    public void deletePermission(String id) {
        log.info("删除权限, id={}", id);
        
        validateDelete(id);
        
        permissionMapper.deleteById(id);
    }

    @Override
    public List<String> getPermissionModules() {
        log.info("获取所有模块");
        
        List<Permission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .select(Permission::getModule)
                        .groupBy(Permission::getModule)
        );
        
        return permissions.stream()
                .map(Permission::getModule)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public PermissionStatisticsVO getStatistics() {
        log.info("获取权限统计信息");
        
        List<Permission> allPermissions = permissionMapper.selectList(new LambdaQueryWrapper<>());
        
        PermissionStatisticsVO statistics = new PermissionStatisticsVO();
        statistics.setTotal(allPermissions.size());
        statistics.setMenuCount((int) allPermissions.stream().filter(p -> "menu".equals(p.getType())).count());
        statistics.setButtonCount((int) allPermissions.stream().filter(p -> "button".equals(p.getType())).count());
        statistics.setApiCount((int) allPermissions.stream().filter(p -> "api".equals(p.getType())).count());
        statistics.setDataCount((int) allPermissions.stream().filter(p -> "data".equals(p.getType())).count());
        
        return statistics;
    }

    @Override
    public boolean checkCodeExists(String code, String excludeId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Permission::getId, excludeId);
        }
        return permissionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasChildren(String id) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getParentId, id);
        return permissionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void validateDelete(String id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        
        if (hasChildren(id)) {
            throw new BusinessException("该权限下还有子权限，无法删除");
        }
    }

    @Override
    public String generatePermissionId() {
        return "PERM" + String.format("%04d", (int) (Math.random() * 10000));
    }

    private List<PermissionVO> buildTree(List<PermissionVO> allPermissions, String parentId) {
        List<PermissionVO> tree = new ArrayList<>();
        
        for (PermissionVO permission : allPermissions) {
            if (parentId == null) {
                if (permission.getParentId() == null) {
                    permission.setChildren(buildTree(allPermissions, permission.getId()));
                    tree.add(permission);
                }
            } else {
                if (parentId.equals(permission.getParentId())) {
                    permission.setChildren(buildTree(allPermissions, permission.getId()));
                    tree.add(permission);
                }
            }
        }
        
        return tree;
    }
}