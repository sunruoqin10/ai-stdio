package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class LeaveCreateRequest {

    @NotBlank(message = "请假类型不能为空")
    private String type;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String attachments;

    @NotBlank(message = "请假事由不能为空")
    private String reason;
}
