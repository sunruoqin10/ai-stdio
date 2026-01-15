package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 部门详情VO
 */
@Data
public class DepartmentDetailVO {
    private String id;
    private String name;
    private String shortName;
    private String parentId;
    private String parentName;
    private String leaderId;
    private String leaderName;
    private String leaderPosition;
    private String leaderPhone;
    private String leaderEmail;
    private Integer level;
    private Integer sort;
    private LocalDate establishedDate;
    private String description;
    private String icon;
    private Integer employeeCount;
    private Integer childCount;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
