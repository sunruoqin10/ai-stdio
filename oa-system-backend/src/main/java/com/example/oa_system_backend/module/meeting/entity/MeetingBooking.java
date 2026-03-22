package com.example.oa_system_backend.module.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_meeting_booking")
public class MeetingBooking {

    @TableId(type = IdType.INPUT)
    private String id;

    private String title;

    private String roomId;

    private String roomName;

    private String roomLocation;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String bookerId;

    private String bookerName;

    private String bookerPosition;

    private String departmentId;

    private String departmentName;

    private Integer participantCount;

    private String participantIds;

    private String agenda;

    private String equipment;

    private String level;

    private String reminder;

    private String recurringRule;

    private String status;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String checkInUser;

    private String checkOutUser;

    private Integer rating;

    private String feedback;

    private String approverId;

    private String approverName;

    private String approvalOpinion;

    private LocalDateTime approvalTime;

    private String rejectionReason;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
