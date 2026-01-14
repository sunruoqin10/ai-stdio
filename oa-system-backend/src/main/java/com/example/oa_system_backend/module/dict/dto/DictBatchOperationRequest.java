package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求DTO
 */
@Data
public class DictBatchOperationRequest {

    /**
     * ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    @Size(min = 1, message = "至少需要一个ID")
    private List<@NotBlank(message = "ID不能为空") String> ids;

    /**
     * 状态(用于批量启用/禁用)
     */
    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;
}
