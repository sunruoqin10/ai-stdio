package com.example.oa_system_backend.module.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApprovalRequest {

    @NotNull(message = "审批状态不能为空")
    @Pattern(regexp = "^(approved|rejected)$", message = "审批状态只能是approved或rejected")
    private String status;

    @Size(max = 500, message = "审批意见不能超过500字符")
    private String opinion;
}
