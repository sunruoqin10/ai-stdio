package com.example.oa_system_backend.module.menu.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuVO {

    private Long id;

    private String menuCode;

    private String menuName;

    private String menuType;

    private Long parentId;

    private Integer menuLevel;

    private String routePath;

    private String componentPath;

    private String redirectPath;

    private String menuIcon;

    private String permission;

    private Integer sortOrder;

    private Boolean visible;

    private Boolean status;

    private Boolean isCache;

    private Boolean isFrame;

    private String frameUrl;

    private String menuTarget;

    private Boolean isSystem;

    private String remark;

    private String createdAt;

    private String updatedAt;

    private List<MenuVO> children;
}
