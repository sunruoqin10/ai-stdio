package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建字典类型请求DTO
 */
@Data
public class DictTypeCreateRequest {

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(min = 2, max = 100, message = "字典编码长度在2-100个字符之间")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$",
             message = "字典编码必须以小写字母开头,只能包含小写字母、数字和下划线")
    private String code;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 2, max = 100, message = "字典名称长度在2-100个字符之间")
    private String name;

    /**
     * 字典描述
     */
    @Size(max = 500, message = "字典描述长度不能超过500个字符")
    private String description;

    /**
     * 字典类别
     */
    @NotBlank(message = "字典类别不能为空")
    @Pattern(regexp = "^(system|business)$", message = "字典类别必须是system或business")
    private String category;

    /**
     * 状态
     */
    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    /**
     * 排序序号
     */
    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    /**
     * 扩展属性(JSON格式)
     */
    private String extProps;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
