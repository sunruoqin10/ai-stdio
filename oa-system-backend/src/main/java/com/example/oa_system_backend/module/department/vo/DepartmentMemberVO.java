package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

/**
 * 部门成员VO
 */
@Data
public class DepartmentMemberVO {
    /**
     * 员工ID
     */
    private String employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工头像
     */
    private String employeeAvatar;

    /**
     * 职位
     */
    private String position;

    /**
     * 员工状态
     */
    private String status;

    /**
     * 是否为负责人
     */
    private Boolean isLeader;

    /**
     * 加入部门日期
     */
    private String joinDepartmentDate;
}
