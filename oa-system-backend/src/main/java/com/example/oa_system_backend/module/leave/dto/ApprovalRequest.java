package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ApprovalRequest {

    @NotBlank(message = "审批状态不能为空")
    private String status;

    private String opinion;
}
