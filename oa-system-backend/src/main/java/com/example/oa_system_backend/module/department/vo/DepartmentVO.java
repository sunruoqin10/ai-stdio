package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门VO
 */
@Data
public class DepartmentVO {
    /**
     * 部门ID
     */
    private String id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门简称
     */
    private String shortName;

    /**
     * 上级部门ID
     */
    private String parentId;

    /**
     * 上级部门名称
     */
    private String parentName;

    /**
     * 部门负责人ID
     */
    private String leaderId;

    /**
     * 部门负责人姓名
     */
    private String leaderName;

    /**
     * 部门层级
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 员工数量
     */
    private Integer employeeCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 子部门列表
     */
    private java.util.List<DepartmentVO> children;
}
