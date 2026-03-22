package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConflictCheckRequest {
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String excludeBookingId;
}
