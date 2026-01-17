package com.example.oa_system_backend.module.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 * 对应表: sys_employee
 */
@Data
@TableName("sys_employee")
public class Employee {

    /**
     * 主键: 员工编号
     * 格式: EMP+YYYYMMDD+序号
     */
    @TableId(type = IdType.INPUT)
    private String id;

    // ========== 基本信息 ==========

    /**
     * 姓名
     */
    private String name;

    /**
     * 英文名
     */
    private String englishName;

    /**
     * 性别: male-男, female-女
     */
    private String gender;

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

    // ========== 工作信息 ==========

    /**
     * 部门ID
     * 外键关联 sys_department.id
     */
    @TableField("department_id")
    private String departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 职级
     */
    private String level;

    /**
     * 直属上级ID
     * 外键关联 sys_employee.id (自关联)
     */
    @TableField("manager_id")
    private String managerId;

    /**
     * 入职日期
     */
    @TableField("join_date")
    private LocalDate joinDate;

    /**
     * 试用期结束日期
     */
    @TableField("probation_end_date")
    private LocalDate probationEndDate;

    /**
     * 工龄(年)
     * 自动计算
     */
    @TableField("work_years")
    private Integer workYears;

    // ========== 状态 ==========

    /**
     * 员工状态
     * active-在职, resigned-离职, suspended-停薪留职
     */
    private String status;

    // ========== 其他信息 ==========

    /**
     * 办公位置
     */
    @TableField("office_location")
    private String officeLocation;

    /**
     * 紧急联系人
     */
    @TableField("emergency_contact")
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    @TableField("emergency_phone")
    private String emergencyPhone;

    // ========== 审计字段 ==========

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 逻辑删除标记
     * 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @TableField("deleted_by")
    private String deletedBy;

    // ========== 关联对象(非数据库字段) ==========

    /**
     * 部门名称(关联查询)
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 上级姓名(关联查询)
     */
    @TableField(exist = false)
    private String managerName;
}
