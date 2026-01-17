package com.example.oa_system_backend.module.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新员工请求DTO
 */
@Data
public class EmployeeUpdateRequest {

    @Size(min = 2, max = 20, message = "姓名长度在2-20个字符之间")
    private String name;

    @Size(max = 50, message = "英文名长度不能超过50个字符")
    private String englishName;

    @Pattern(regexp = "^(male|female)$", message = "性别必须是male或female")
    private String gender;

    private LocalDate birthDate;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "请输入正确的邮箱格式")
    private String email;

    private String avatar;

    private String departmentId;

    @Size(min = 2, max = 50, message = "职位长度在2-50个字符之间")
    private String position;

    private String level;

    private String managerId;

    private LocalDate joinDate;

    private LocalDate probationEndDate;

    private String officeLocation;

    private String emergencyContact;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String emergencyPhone;
}
