package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private String id;
    private String type;
    private String title;
    private String content;
    private String bookingId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
