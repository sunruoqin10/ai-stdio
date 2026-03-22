package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

@Data
public class RoomUsageStatsVO {
    private String roomId;
    private String roomName;
    private Integer bookingCount;
    private Integer totalHours;
    private Double utilizationRate;
}
