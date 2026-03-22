package com.example.oa_system_backend.module.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.menu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> selectMenuTree(@Param("parentId") Long parentId);

    List<Menu> selectMenuList(@Param("menuType") String menuType,
                              @Param("status") String status,
                              @Param("keyword") String keyword);

    Menu selectByMenuCode(@Param("menuCode") String menuCode);

    Integer countByParentId(@Param("parentId") Long parentId);

    List<Menu> selectParentOptions();

    List<Menu> selectMenuRoutes();
}
