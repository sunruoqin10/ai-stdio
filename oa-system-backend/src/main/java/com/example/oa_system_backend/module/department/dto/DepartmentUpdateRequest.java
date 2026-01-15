package com.example.oa_system_backend.module.department.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 部门更新请求DTO
 */
@Data
public class DepartmentUpdateRequest {

    /**
     * 部门名称
     */
    @Size(min = 2, max = 50, message = "部门名称长度在2-50个字符之间")
    private String name;

    /**
     * 部门简称
     */
    @Size(min = 2, max = 20, message = "部门简称长度在2-20个字符之间")
    private String shortName;

    /**
     * 部门负责人ID
     */
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

    /**
     * 乐观锁版本号（暂时不使用，保留字段供未来扩展）
     */
    // @NotNull(message = "版本号不能为空")  // 暂时移除验证
    private Integer version;
}
