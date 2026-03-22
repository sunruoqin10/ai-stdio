package com.example.oa_system_backend.module.menu.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.menu.dto.*;
import com.example.oa_system_backend.module.menu.entity.Menu;
import com.example.oa_system_backend.module.menu.service.MenuService;
import com.example.oa_system_backend.module.menu.vo.MenuVO;
import com.example.oa_system_backend.module.menu.vo.RouteMenuItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<List<MenuVO>> getMenuList(MenuQueryRequest request) {
        List<MenuVO> tree = menuService.getMenuTree(0L);
        return ApiResponse.success(tree);
    }

    @GetMapping("/{id}")
    public ApiResponse<MenuVO> getMenuById(@PathVariable Long id) {
        MenuVO menu = menuService.getMenuById(id);
        return ApiResponse.success(menu);
    }

    @PostMapping
    public ApiResponse<Menu> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        Menu menu = menuService.createMenu(request);
        return ApiResponse.success("创建成功", menu);
    }

    @PutMapping("/{id}")
    public ApiResponse<Menu> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuUpdateRequest request) {
        Menu menu = menuService.updateMenu(id, request);
        return ApiResponse.success("更新成功", menu);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/parent-options")
    public ApiResponse<List<MenuVO>> getParentOptions() {
        List<MenuVO> options = menuService.getParentOptions();
        return ApiResponse.success(options);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Menu> toggleMenuStatus(
            @PathVariable Long id,
            @Valid @RequestBody MenuStatusUpdateRequest request) {
        Menu menu = menuService.updateMenuStatus(id, request);
        return ApiResponse.success("状态更新成功", menu);
    }

    @GetMapping("/routes")
    public ApiResponse<List<RouteMenuItem>> getMenuRoutes() {
        List<RouteMenuItem> routes = menuService.getMenuRoutes();
        return ApiResponse.success(routes);
    }
}
