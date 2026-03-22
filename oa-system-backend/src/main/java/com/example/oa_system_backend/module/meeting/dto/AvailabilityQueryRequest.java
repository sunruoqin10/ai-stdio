package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

@Data
public class AvailabilityQueryRequest {
    private String roomId;
    private String date;
}
