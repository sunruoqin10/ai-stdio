package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

@Data
public class TimeSlotStatsVO {
    private String timeSlot;
    private Integer morningCount;
    private Integer afternoonCount;
    private Integer eveningCount;
}
