package com.example.oa_system_backend.module.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String menuCode;

    private String menuName;

    private String menuType;

    private Long parentId;

    private Integer menuLevel;

    private String routePath;

    private String componentPath;

    private String redirectPath;

    private String icon;

    private String permission;

    private Integer sort;

    private Integer visible;

    private String status;

    private Integer isCache;

    private Integer isFrame;

    private String frameUrl;

    private String menuTarget;

    private Integer isSystem;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("deleted_by")
    private String deletedBy;
}
