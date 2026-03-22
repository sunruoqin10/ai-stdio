package com.example.oa_system_backend.module.menu.service;

import com.example.oa_system_backend.module.menu.dto.*;
import com.example.oa_system_backend.module.menu.entity.Menu;
import com.example.oa_system_backend.module.menu.vo.MenuVO;
import com.example.oa_system_backend.module.menu.vo.RouteMenuItem;

import java.util.List;

public interface MenuService {

    List<MenuVO> getMenuTree(Long parentId);

    MenuVO getMenuById(Long id);

    Menu createMenu(MenuCreateRequest request);

    Menu updateMenu(Long id, MenuUpdateRequest request);

    void deleteMenu(Long id);

    List<MenuVO> getParentOptions();

    Menu updateMenuStatus(Long id, MenuStatusUpdateRequest request);

    List<RouteMenuItem> getMenuRoutes();
}
