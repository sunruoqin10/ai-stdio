package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BalanceUpdateRequest {

    @NotBlank(message = "员工ID不能为空")
    private String employeeId;

    @NotNull(message = "年份不能为空")
    private Integer year;

    @NotNull(message = "年假总额不能为空")
    @DecimalMin(value = "0", message = "年假总额不能小于0")
    private BigDecimal annualTotal;
}
