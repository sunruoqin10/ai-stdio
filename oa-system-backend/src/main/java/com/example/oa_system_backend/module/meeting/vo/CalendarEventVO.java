package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarEventVO {
    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String resourceId;
    private String status;
    private String organizer;
    private String department;
    private String level;
    private Integer attendeeCount;
}
