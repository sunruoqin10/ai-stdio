package com.example.oa_system_backend.module.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("meeting_attendee")
public class MeetingAttendee {

    @TableId(type = IdType.INPUT)
    private String id;

    private String bookingId;

    private String userId;

    private String userName;

    private String departmentId;

    private String departmentName;

    private Integer isRequired;

    private String status;

    private LocalDateTime responseTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
