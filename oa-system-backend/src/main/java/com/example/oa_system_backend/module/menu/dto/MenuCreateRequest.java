package com.example.oa_system_backend.module.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuCreateRequest {

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    private String routePath;

    private String componentPath;

    private String redirectPath;

    private String icon;

    private String permission;

    private Integer sortOrder;

    private Boolean visible;

    private Boolean status;

    private Boolean isCache;

    private Boolean isFrame;

    private String frameUrl;

    private String menuTarget;

    private String remark;
}
