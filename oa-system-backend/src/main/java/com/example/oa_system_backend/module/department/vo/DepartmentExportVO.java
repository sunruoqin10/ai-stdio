package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

@Data
public class DepartmentExportVO {
    private String id;
    private String name;
    private String shortName;
    private String parentId;
    private String parentName;
    private String leaderName;
    private Integer level;
    private Integer sort;
    private String establishedDate;
    private String description;
    private String status;
    private String createdAt;
}