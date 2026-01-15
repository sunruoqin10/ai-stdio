package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

/**
 * 部门统计VO
 */
@Data
public class DepartmentStatisticsVO {
    /**
     * 总部门数
     */
    private Integer totalCount;

    /**
     * 一级部门数
     */
    private Integer level1Count;

    /**
     * 二级部门数
     */
    private Integer level2Count;

    /**
     * 三级部门数
     */
    private Integer level3Count;

    /**
     * 四级部门数
     */
    private Integer level4Count;

    /**
     * 最大层级
     */
    private Integer maxLevel;

    /**
     * 有负责人的部门数
     */
    private Integer withLeaderCount;

    /**
     * 总员工数
     */
    private Integer totalEmployees;

    /**
     * 启用部门数
     */
    private Integer activeDepartmentCount;

    /**
     * 停用部门数
     */
    private Integer disabledDepartmentCount;
}
