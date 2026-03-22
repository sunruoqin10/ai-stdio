package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendeeVO {
    private String userId;
    private String userName;
    private String departmentName;
    private Boolean required;
    private String status;
    private LocalDateTime responseTime;
}
