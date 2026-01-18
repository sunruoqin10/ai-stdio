package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class HolidayCreateRequest {

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotBlank(message = "节假日名称不能为空")
    private String name;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotNull(message = "年份不能为空")
    private Integer year;

    private Integer isWorkday;
}
