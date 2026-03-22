package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingForm {
    private String id;
    private String title;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bookerId;
    private String bookerName;
    private String departmentId;
    private String departmentName;
    private String agenda;
    private Integer participantCount;
    private List<String> participantIds;
    private List<String> equipment;
    private String level;
    private String reminder;
    private String recurringRule;
}
