package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvailabilityVO {
    private String date;
    private String startTime;
    private String endTime;
    private Boolean available;
    private List<BookingVO> conflicts;
}
