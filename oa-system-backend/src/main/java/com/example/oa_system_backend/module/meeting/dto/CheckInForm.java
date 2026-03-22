package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckInForm {
    private String bookingId;
    private LocalDateTime actualStartTime;
}
