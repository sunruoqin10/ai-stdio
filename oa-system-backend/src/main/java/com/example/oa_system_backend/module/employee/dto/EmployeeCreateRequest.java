package com.example.oa_system_backend.module.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建员工请求DTO
 */
@Data
public class EmployeeCreateRequest {

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符之间")
    private String name;

    /**
     * 英文名
     */
    @Size(max = 50, message = "英文名长度不能超过50个字符")
    private String englishName;

    /**
     * 性别
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别必须是male或female")
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 部门ID
     */
    @NotBlank(message = "部门不能为空")
    private String departmentId;

    /**
     * 职位
     */
    @NotBlank(message = "职位不能为空")
    @Size(min = 2, max = 50, message = "职位长度在2-50个字符之间")
    private String position;

    /**
     * 职级
     */
    private String level;

    /**
     * 直属上级ID
     */
    private String managerId;

    /**
     * 入职日期
     */
    @NotNull(message = "入职日期不能为空")
    @PastOrPresent(message = "入职日期不能晚于今天")
    private LocalDate joinDate;

    /**
     * 试用期结束日期
     */
    private LocalDate probationEndDate;

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
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String emergencyPhone;
}
