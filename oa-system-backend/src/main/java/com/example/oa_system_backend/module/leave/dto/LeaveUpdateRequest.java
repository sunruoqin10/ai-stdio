package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeaveUpdateRequest {

    private String type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<String> attachments;

    private String reason;

    @NotNull(message = "版本号不能为空")
    private Integer version;
}
