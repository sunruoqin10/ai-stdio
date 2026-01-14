package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新字典类型请求DTO
 */
@Data
public class DictTypeUpdateRequest {

    @Size(min = 2, max = 100, message = "字典名称长度在2-100个字符之间")
    private String name;

    @Size(max = 500, message = "字典描述长度不能超过500个字符")
    private String description;

    @Pattern(regexp = "^(system|business)$", message = "字典类别必须是system或business")
    private String category;

    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    private String extProps;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
