package com.example.oa_system_backend.module.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.menu.dto.*;
import com.example.oa_system_backend.module.menu.entity.Menu;
import com.example.oa_system_backend.module.menu.mapper.MenuMapper;
import com.example.oa_system_backend.module.menu.service.MenuService;
import com.example.oa_system_backend.module.menu.vo.MenuVO;
import com.example.oa_system_backend.module.menu.vo.RouteMenuItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<MenuVO> getMenuTree(Long parentId) {
        List<Menu> menus = menuMapper.selectMenuTree(parentId);
        return menus.stream()
                .map(this::convertToVO)
                .peek(vo -> {
                    List<MenuVO> children = getMenuTree(vo.getId());
                    vo.setChildren(children);
                })
                .collect(Collectors.toList());
    }

    @Override
    public MenuVO getMenuById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return convertToVO(menu);
    }

    @Override
    @Transactional
    public Menu createMenu(MenuCreateRequest request) {
        Long parentId = request.getParentId();
        if (parentId != 0) {
            Menu parentMenu = menuMapper.selectById(parentId);
            if (parentMenu == null) {
                throw new BusinessException("父菜单不存在");
            }
        }

        Menu menu = new Menu();
        menu.setMenuName(request.getMenuName());
        menu.setMenuType(request.getMenuType());
        menu.setParentId(request.getParentId());
        menu.setRoutePath(request.getRoutePath());
        menu.setComponentPath(request.getComponentPath());
        menu.setRedirectPath(request.getRedirectPath());
        menu.setIcon(request.getIcon());
        menu.setPermission(request.getPermission());

        String menuCode = generateNextMenuCode();
        menu.setMenuCode(menuCode);

        menu.setMenuLevel(parentId == 0 ? 1 : 2);
        menu.setIsSystem(0);

        if (request.getVisible() == null) {
            menu.setVisible(1);
        } else {
            menu.setVisible(request.getVisible() ? 1 : 0);
        }
        if (request.getIsCache() == null) {
            menu.setIsCache(1);
        } else {
            menu.setIsCache(request.getIsCache() ? 1 : 0);
        }
        if (request.getIsFrame() == null) {
            menu.setIsFrame(0);
        } else {
            menu.setIsFrame(request.getIsFrame() ? 1 : 0);
        }

        Boolean status = request.getStatus();
        if (status == null) {
            menu.setStatus("enabled");
        } else {
            menu.setStatus(status ? "enabled" : "disabled");
        }

        if (request.getSortOrder() == null) {
            menu.setSort(0);
        } else {
            menu.setSort(request.getSortOrder());
        }

        menu.setMenuTarget(request.getMenuTarget());
        menu.setFrameUrl(request.getFrameUrl());
        menu.setRemark(request.getRemark());

        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());

        menuMapper.insert(menu);

        return menu;
    }

    @Override
    @Transactional
    public Menu updateMenu(Long id, MenuUpdateRequest request) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        if (menu.getIsSystem() == 1) {
            throw new BusinessException("系统菜单不允许修改");
        }

        if (request.getParentId() == id) {
            throw new BusinessException("不能将菜单设置为自己的父菜单");
        }

        Long newParentId = request.getParentId();
        if (newParentId != 0 && !newParentId.equals(menu.getParentId())) {
            Menu parentMenu = menuMapper.selectById(newParentId);
            if (parentMenu == null) {
                throw new BusinessException("父菜单不存在");
            }
            menu.setParentId(newParentId);
            menu.setMenuLevel(newParentId == 0 ? 1 : 2);
        }

        menu.setMenuName(request.getMenuName());
        menu.setMenuType(request.getMenuType());
        menu.setRoutePath(request.getRoutePath());
        menu.setComponentPath(request.getComponentPath());
        menu.setRedirectPath(request.getRedirectPath());
        menu.setIcon(request.getIcon());
        menu.setPermission(request.getPermission());
        menu.setSort(request.getSortOrder());
        if (request.getVisible() != null) {
            menu.setVisible(request.getVisible() ? 1 : 0);
        }
        if (request.getStatus() != null) {
            menu.setStatus(request.getStatus() ? "enabled" : "disabled");
        }
        if (request.getIsCache() != null) {
            menu.setIsCache(request.getIsCache() ? 1 : 0);
        }
        if (request.getIsFrame() != null) {
            menu.setIsFrame(request.getIsFrame() ? 1 : 0);
        }
        menu.setFrameUrl(request.getFrameUrl());
        menu.setMenuTarget(request.getMenuTarget());
        menu.setRemark(request.getRemark());

        menu.setUpdatedAt(LocalDateTime.now());

        menuMapper.updateById(menu);

        return menu;
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        if (menu.getIsSystem() == 1) {
            throw new BusinessException("系统菜单不允许删除");
        }

        if (menuMapper.countByParentId(id) > 0) {
            throw new BusinessException("该菜单存在子菜单，无法删除");
        }

        menuMapper.deleteById(id);
    }

    @Override
    public List<MenuVO> getParentOptions() {
        List<Menu> menus = menuMapper.selectParentOptions();
        return menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Menu updateMenuStatus(Long id, MenuStatusUpdateRequest request) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        if (menu.getIsSystem() == 1) {
            throw new BusinessException("系统菜单不允许修改");
        }

        menu.setStatus(request.getStatus() ? "enabled" : "disabled");
        menu.setUpdatedAt(LocalDateTime.now());

        menuMapper.updateById(menu);

        return menu;
    }

    @Override
    public List<RouteMenuItem> getMenuRoutes() {
        List<Menu> menus = menuMapper.selectMenuRoutes();
        return buildRouteTree(menus, 0L);
    }

    private List<RouteMenuItem> buildRouteTree(List<Menu> menus, Long parentId) {
        List<RouteMenuItem> routes = new ArrayList<>();

        for (Menu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                RouteMenuItem route = convertToRouteItem(menu);

                List<RouteMenuItem> children = buildRouteTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    route.setChildren(children);
                }

                routes.add(route);
            }
        }

        return routes;
    }

    private MenuVO convertToVO(Menu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setMenuCode(menu.getMenuCode());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuType(menu.getMenuType());
        vo.setParentId(menu.getParentId());
        vo.setMenuLevel(menu.getMenuLevel());
        vo.setRoutePath(menu.getRoutePath());
        vo.setComponentPath(menu.getComponentPath());
        vo.setRedirectPath(menu.getRedirectPath());
        vo.setMenuIcon(menu.getIcon());
        vo.setPermission(menu.getPermission());
        vo.setSortOrder(menu.getSort());
        vo.setVisible(menu.getVisible() != null && menu.getVisible() == 1);
        vo.setStatus("enabled".equals(menu.getStatus()));
        vo.setIsCache(menu.getIsCache() != null && menu.getIsCache() == 1);
        vo.setIsFrame(menu.getIsFrame() != null && menu.getIsFrame() == 1);
        vo.setFrameUrl(menu.getFrameUrl());
        vo.setMenuTarget(menu.getMenuTarget());
        vo.setIsSystem(menu.getIsSystem() != null && menu.getIsSystem() == 1);
        vo.setRemark(menu.getRemark());
        if (menu.getCreatedAt() != null) {
            vo.setCreatedAt(menu.getCreatedAt().toString());
        }
        if (menu.getUpdatedAt() != null) {
            vo.setUpdatedAt(menu.getUpdatedAt().toString());
        }
        return vo;
    }

    private RouteMenuItem convertToRouteItem(Menu menu) {
        RouteMenuItem route = new RouteMenuItem();
        route.setPath(menu.getRoutePath());
        route.setName(menu.getMenuCode());
        route.setComponent(menu.getComponentPath());
        route.setRedirect(menu.getRedirectPath());

        RouteMenuItem.RouteMeta meta = new RouteMenuItem.RouteMeta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setHidden(menu.getVisible() == 0);
        meta.setKeepAlive(menu.getIsCache() == 1);
        if (menu.getPermission() != null && !menu.getPermission().isEmpty()) {
            meta.setPermissions(new String[]{menu.getPermission()});
        }
        route.setMeta(meta);

        return route;
    }

    private String generateNextMenuCode() {
        String prefix = "MENU";

        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.likeRight("menu_code", prefix);
        wrapper.orderByDesc("menu_code");
        wrapper.last("LIMIT 1");

        Menu lastMenu = menuMapper.selectOne(wrapper);

        if (lastMenu == null) {
            return prefix + "0001";
        }

        String lastCode = lastMenu.getMenuCode();
        String numStr = lastCode.substring(prefix.length());
        int num = Integer.parseInt(numStr);
        return prefix + String.format("%04d", num + 1);
    }
}
