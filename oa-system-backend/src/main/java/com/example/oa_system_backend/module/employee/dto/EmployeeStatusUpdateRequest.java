package com.example.oa_system_backend.module.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 员工状态更新请求DTO
 */
@Data
public class EmployeeStatusUpdateRequest {

    /**
     * 新状态
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|resigned|suspended)$",
             message = "状态必须是active、resigned或suspended")
    private String status;

    /**
     * 原因(离职原因等)
     */
    private String reason;
}
