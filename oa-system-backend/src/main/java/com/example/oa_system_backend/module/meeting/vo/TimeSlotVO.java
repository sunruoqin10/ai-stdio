package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

@Data
public class TimeSlotVO {
    private String time;
    private Integer bookingCount;
    private Integer totalDuration;
}
