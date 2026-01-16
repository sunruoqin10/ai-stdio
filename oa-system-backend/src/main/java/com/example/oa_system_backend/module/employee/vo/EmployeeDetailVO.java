package com.example.oa_system_backend.module.employee.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工详情VO
 */
@Data
public class EmployeeDetailVO {

    /**
     * 员工编号
     */
    private String id;

    /**
     * 员工编号（别名，用于前端显示）
     */
    private String employeeNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 英文名
     */
    private String englishName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 性别显示名称
     */
    private String genderLabel;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 职位
     */
    private String position;

    /**
     * 职位显示名称
     */
    private String positionLabel;

    /**
     * 职级
     */
    private String level;

    /**
     * 职级显示名称
     */
    private String levelLabel;

    /**
     * 直属上级ID
     */
    private String managerId;

    /**
     * 直属上级姓名
     */
    private String managerName;

    /**
     * 入职日期
     */
    private LocalDate joinDate;

    /**
     * 试用期状态
     */
    private String probationStatus;

    /**
     * 试用期状态显示名称
     */
    private String probationStatusLabel;

    /**
     * 试用期结束日期
     */
    private LocalDate probationEndDate;

    /**
     * 工龄(年)
     */
    private Integer workYears;

    /**
     * 员工状态
     */
    private String status;

    /**
     * 员工状态显示名称
     */
    private String statusLabel;

    /**
     * 办公位置
     */
    private String officeLocation;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    private String updatedBy;
}
