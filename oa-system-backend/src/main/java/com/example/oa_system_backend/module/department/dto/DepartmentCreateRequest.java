package com.example.oa_system_backend.module.department.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 部门创建请求DTO
 */
@Data
public class DepartmentCreateRequest {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 50, message = "部门名称长度在2-50个字符之间")
    private String name;

    /**
     * 部门简称
     */
    @Size(min = 2, max = 20, message = "部门简称长度在2-20个字符之间")
    private String shortName;

    /**
     * 上级部门ID
     */
    private String parentId;

    /**
     * 部门负责人ID
     */
    @NotBlank(message = "部门负责人不能为空")
    private String leaderId;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能小于0")
    private Integer sort;

    /**
     * 成立时间
     */
    private LocalDate establishedDate;

    /**
     * 部门描述
     */
    @Size(max = 500, message = "部门描述最多500个字符")
    private String description;

    /**
     * 部门图标URL
     */
    @Size(max = 500, message = "部门图标URL最多500个字符")
    private String icon;

    /**
     * 状态
     */
    private String status;
}
