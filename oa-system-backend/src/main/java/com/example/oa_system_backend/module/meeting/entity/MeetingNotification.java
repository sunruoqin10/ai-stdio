package com.example.oa_system_backend.module.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("meeting_notification")
public class MeetingNotification {

    @TableId(type = IdType.INPUT)
    private String id;

    private String recipientId;

    private String recipientName;

    private String type;

    private String title;

    private String content;

    private String bookingId;

    private Integer isRead;

    private LocalDateTime readAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
