package com.example.oa_system_backend.module.employee.vo;

import lombok.Data;

import java.util.List;

/**
 * 员工统计数据VO
 */
@Data
public class EmployeeStatisticsVO {

    /**
     * 总员工数
     */
    private Integer total;

    /**
     * 在职人数
     */
    private Integer active;

    /**
     * 离职人数
     */
    private Integer resigned;

    /**
     * 停薪留职人数
     */
    private Integer suspended;

    /**
     * 试用期人数
     */
    private Integer probation;

    /**
     * 本月新入职
     */
    private Integer newThisMonth;

    /**
     * 部门分布
     */
    private List<DepartmentCountVO> byDepartment;

    /**
     * 部门人数统计
     */
    @Data
    public static class DepartmentCountVO {
        /**
         * 部门ID
         */
        private String departmentId;

        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 人数
         */
        private Integer count;
    }
}
